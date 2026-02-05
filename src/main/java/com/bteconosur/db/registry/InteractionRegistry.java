package com.bteconosur.db.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.InteractionKey;
import com.bteconosur.discord.action.AcceptCreateProjectAction;
import com.bteconosur.discord.action.ButtonAction;
import com.bteconosur.discord.action.CreateProjectAction;
import com.bteconosur.discord.action.JoinProjectAction;
import com.bteconosur.discord.action.ModalAction;
import com.bteconosur.discord.action.RejectCreateProjectAction;
import com.bteconosur.discord.action.SelectAction;
import com.bteconosur.discord.util.MessageService;

public class InteractionRegistry extends Registry<Long, Interaction> {

    private static InteractionRegistry instance;

    private final Map<InteractionKey, ModalAction> modalActions = new HashMap<>();
    private final Map<InteractionKey, ButtonAction> buttonActions = new HashMap<>();
    private final Map<InteractionKey, SelectAction> selectActions = new HashMap<>();

    public InteractionRegistry() {
        super();
        ConsoleLogger.info(lang.getString("interaction-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<Interaction> interactions = dbManager.selectAll(Interaction.class);
        if (interactions != null) {
            for (Interaction interaction : interactions) {
                loadedObjects.put(interaction.getId(), interaction);
            }
        }

        registerButtonAction(InteractionKey.CREATE_PROJECT, new CreateProjectAction());
        registerModalAction(InteractionKey.ACCEPT_CREATE_PROJECT, new AcceptCreateProjectAction());
        registerModalAction(InteractionKey.REJECT_CREATE_PROJECT, new RejectCreateProjectAction());
        registerButtonAction(InteractionKey.JOIN_PROJECT, new JoinProjectAction());

        try {
            int expirationMinutes = config.getInt("interaction-expiration");
            long periodTicks = 20L * 60L * expirationMinutes;
            BTEConoSur.getInstance().getServer().getScheduler().runTaskTimer(BTEConoSur.getInstance(), () -> {
                purgeExpired();
            }, periodTicks, periodTicks);
            ConsoleLogger.info("Tarea de purga de interacciones programada cada " + expirationMinutes + " minutos.");
        } catch (Exception e) {
            ConsoleLogger.warn("No se pudo programar la purga periódica de interacciones: " + e.getMessage());
        }
    }

    @Override
    public void load(Interaction obj) {
        if (obj == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
    }

    @Override
    public void shutdown() {
        ConsoleLogger.info(lang.getString("interaction-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public void purgeExpired() {
        ProjectManager pm = ProjectManager.getInstance();
        for (Interaction interaction : loadedObjects.values()) {
            if (interaction.isExpired()) {
                if (interaction.getInteractionKey() == InteractionKey.CREATE_PROJECT) pm.expiredCreateRequest(interaction.getProjectId(), interaction.getId());
                else if (interaction.getInteractionKey() == InteractionKey.JOIN_PROJECT) pm.expiredJoinRequest(interaction.getProjectId(), interaction.getPlayerId());
                else if (interaction.getInteractionKey() == InteractionKey.FINISH_PROJECT) pm.expiredFinishRequest(interaction.getProjectId());
                else if (interaction.getInteractionKey() == InteractionKey.REDEFINE_PROJECT) pm.expiredRedefineRequest(interaction.getProjectId());
                else if (interaction.getInteractionKey() == InteractionKey.EDIT_PROJECT) pm.expiredEditRequest(interaction.getProjectId());
                else unload(interaction.getId()); //TODO: testear esto
            }
        }
        loadedObjects.entrySet().removeIf(entry -> {
            Interaction interaction = entry.getValue();
            boolean expired = interaction.isExpired();
            if (expired) dbManager.remove(interaction);
            return expired;
        });
    }
    
    @Override
    public void unload(Long id) {
        if (id == null) return;
        Interaction interaction = loadedObjects.get(id);
        if (interaction == null || interaction.getId() == null) return;
        if (interaction.getComponentId() != null) interaction.setComponentId(null);
        if (interaction.getInteractionKey() == InteractionKey.CREATE_PROJECT) {
            Proyecto proyecto = ProyectoRegistry.getInstance().get(interaction.getProjectId());
            Pais pais = proyecto.getPais();
            MessageService.deleteMessage(pais.getDsIdRequest(), interaction.getMessageId());
        }
        if (interaction.getInteractionKey() == InteractionKey.JOIN_PROJECT && interaction.getMessageId() != null) {
            Proyecto proyecto = ProyectoRegistry.getInstance().get(interaction.getProjectId());
            Player player = ProjectManager.getInstance().getLider(proyecto);
            MessageService.deleteDMMessage(player.getDsIdUsuario(), interaction.getMessageId());
        }
        loadedObjects.remove(interaction.getId());
        dbManager.remove(interaction);
    }

    public Interaction findCreateRequest(Proyecto project) {
        if (project == null) return null;
        return findByInteractionKey(InteractionKey.CREATE_PROJECT)
            .stream()
            .filter(interaction -> project.getId().equals(interaction.getProjectId()))
            .findFirst()
            .orElse(null);
    }

    public Interaction findCreateRequest(Player player) {
        if (player == null) return null;
        return findByInteractionKey(InteractionKey.CREATE_PROJECT)
            .stream()
            .filter(interaction -> player.getUuid().equals(interaction.getPlayerId()))
            .findFirst()
            .orElse(null);
    }

    public List<Interaction> findByInteractionKey(InteractionKey key) {
        if (key == null) return null;
        List<Interaction> results = new ArrayList<>();
        for (Interaction interaction : loadedObjects.values()) {
            if (key.equals(interaction.getInteractionKey())) results.add(interaction);
        }
        return results;
    }

    public List<Interaction> findJoinRequest(String proyectoId) {
        if (proyectoId == null) return null;
        List<Interaction> results = findByInteractionKey(InteractionKey.JOIN_PROJECT)
            .stream()
            .filter(interaction -> proyectoId.equals(interaction.getProjectId()))
            .toList();
        return results;
    }

    public Interaction findJoinRequest(String proyectoId, UUID playerId) {
        if (proyectoId == null || playerId == null) return null;
        return findByInteractionKey(InteractionKey.JOIN_PROJECT)
            .stream()
            .filter(interaction -> proyectoId.equals(interaction.getProjectId()) && playerId.equals(interaction.getPlayerId()))
            .findFirst()
            .orElse(null);
    }

    public Interaction findFinishRequest(Proyecto project) {
        if (project == null) return null;
        return findByInteractionKey(InteractionKey.FINISH_PROJECT)
            .stream()
            .filter(interaction -> project.getId().equals(interaction.getProjectId()))
            .findFirst()
            .orElse(null);
    }

    public Interaction findRedefineRequest(Proyecto project) {
        if (project == null) return null;
        return findByInteractionKey(InteractionKey.REDEFINE_PROJECT)
            .stream()
            .filter(interaction -> project.getId().equals(interaction.getProjectId()))
            .findFirst()
            .orElse(null);  
    }

    public Interaction findEditRequest(Proyecto project) {
        if (project == null) return null;
        return findByInteractionKey(InteractionKey.EDIT_PROJECT)
            .stream()
            .filter(interaction -> project.getId().equals(interaction.getProjectId()))
            .findFirst()
            .orElse(null);
    }

    public Interaction findByComponentId(String componentId) {
        if (componentId == null) return null;
        for (Interaction interaction : loadedObjects.values()) {
            if (componentId.equals(interaction.getComponentId())) return interaction;
        }
        return null;
    }

    public Interaction findByMessageId(Long messageId) {
        if (messageId == null) return null;
        for (Interaction interaction : loadedObjects.values()) {
            if (messageId.equals(interaction.getMessageId())) return interaction;
        }
        return null;
    }

    public void registerModalAction(InteractionKey key, ModalAction action) {
        modalActions.put(key, action);
    }

    public void registerButtonAction(InteractionKey key, ButtonAction action) {
        buttonActions.put(key, action);
    }

    public void registerSelectAction(InteractionKey key, SelectAction action) {
        selectActions.put(key, action);
    }

    public ModalAction getModalAction(InteractionKey key) {
        return modalActions.get(key);
    }

    public ButtonAction getButtonAction(InteractionKey key) {
        return buttonActions.get(key);
    }

    public SelectAction getSelectAction(InteractionKey key) {
        return selectActions.get(key);
    }

    public static InteractionRegistry getInstance() {
        if (instance == null) {
            instance = new InteractionRegistry();
        }
        return instance;
    }
}
