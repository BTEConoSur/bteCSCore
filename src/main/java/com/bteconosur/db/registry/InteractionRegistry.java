package com.bteconosur.db.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.InteractionKey;
import com.bteconosur.discord.action.AcceptCreateProjectAction;
import com.bteconosur.discord.action.AcceptRedefineProjectAction;
import com.bteconosur.discord.action.ButtonAction;
import com.bteconosur.discord.action.CreateProjectAction;
import com.bteconosur.discord.action.JoinProjectAction;
import com.bteconosur.discord.action.ModalAction;
import com.bteconosur.discord.action.RedefineProjectAction;
import com.bteconosur.discord.action.RejectCreateProjectAction;
import com.bteconosur.discord.action.RejectRedefineProjectAction;
import com.bteconosur.discord.action.SelectAction;
import com.bteconosur.discord.util.MessageService;

public class InteractionRegistry extends Registry<Long, Interaction> {

    private static InteractionRegistry instance;

    private final Map<InteractionKey, ModalAction> modalActions = new HashMap<>();
    private final Map<InteractionKey, ButtonAction> buttonActions = new HashMap<>();
    private final Map<InteractionKey, SelectAction> selectActions = new HashMap<>();

    public InteractionRegistry() {
        super();
        ConsoleLogger.info(LanguageHandler.getText("interaction-registry-initializing"));
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
        registerButtonAction(InteractionKey.REDEFINE_PROJECT, new RedefineProjectAction());
        registerModalAction(InteractionKey.ACCEPT_REDEFINE_PROJECT, new AcceptRedefineProjectAction());
        registerModalAction(InteractionKey.REJECT_REDEFINE_PROJECT, new RejectRedefineProjectAction());
        try {
            int expirationMinutes = config.getInt("interaction-expiration");
            long periodTicks = 20L * 60L * expirationMinutes;
            BTEConoSur.getInstance().getServer().getScheduler().runTaskTimer(BTEConoSur.getInstance(), () -> {
                purgeExpired();
            }, periodTicks, periodTicks);
            ConsoleLogger.info(LanguageHandler.getText("ds-purge-scheduled").replace("%minutes%", String.valueOf(expirationMinutes)));
        } catch (Exception e) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-purge-failed") + e.getMessage());
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
        ConsoleLogger.info(LanguageHandler.getText("interaction-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public void purgeExpired() {
        ProjectManager pm = ProjectManager.getInstance();
        ConsoleLogger.debug("Purgando interacciones.");
        for (Interaction interaction : loadedObjects.values()) {
            if (interaction.isExpired()) {
                if (interaction.getInteractionKey() == InteractionKey.CREATE_PROJECT) pm.expiredCreateRequest(interaction.getProjectId(), interaction.getId());
                else if (interaction.getInteractionKey() == InteractionKey.JOIN_PROJECT) pm.expiredJoinRequest(interaction.getProjectId(), interaction.getPlayerId());
                else if (interaction.getInteractionKey() == InteractionKey.FINISH_PROJECT) pm.expiredFinishRequest(interaction.getProjectId());
                else if (interaction.getInteractionKey() == InteractionKey.REDEFINE_PROJECT) pm.expiredRedefineRequest(interaction.getProjectId(), interaction.getId());
                else if (interaction.getInteractionKey() == InteractionKey.FINISH_EDIT_PROJECT) pm.expiredFinishEditRequest(interaction.getProjectId());
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
            if (player != null) MessageService.deleteDMMessage(player.getDsIdUsuario(), interaction.getMessageId());
            else {
                Long liderDsId = (Long) interaction.getPayloadValue("liderDsId");
                if (liderDsId != null) {
                    MessageService.deleteDMMessage(liderDsId, interaction.getMessageId());
                }
            }
            
        }
        if (interaction.getInteractionKey() == InteractionKey.REDEFINE_PROJECT) {
            Proyecto proyecto = ProyectoRegistry.getInstance().get(interaction.getProjectId());
            Pais pais = proyecto.getPais();
            MessageService.deleteMessage(pais.getDsIdRequest(), interaction.getMessageId());
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

    public int countJoinRequests(String proyectoId) {
        if (proyectoId == null) return 0;
        return (int) findByInteractionKey(InteractionKey.JOIN_PROJECT)
            .stream()
            .filter(interaction -> proyectoId.equals(interaction.getProjectId()))
            .count();
    }

    public Interaction findFinishRequest(String proyectoId) {
        if (proyectoId == null) return null;
        return findByInteractionKey(InteractionKey.FINISH_PROJECT)
            .stream()
            .filter(interaction -> proyectoId.equals(interaction.getProjectId()))
            .findFirst()
            .orElse(null);
    }

    public Interaction findRedefineRequest(String proyectoId) {
        if (proyectoId == null) return null;
        return findByInteractionKey(InteractionKey.REDEFINE_PROJECT)
            .stream()
            .filter(interaction -> proyectoId.equals(interaction.getProjectId()))
            .findFirst()
            .orElse(null);  
    }

    public Interaction findFinishEditRequest(String proyectoId) {
        if (proyectoId == null) return null;
        return findByInteractionKey(InteractionKey.FINISH_EDIT_PROJECT)
            .stream()
            .filter(interaction -> proyectoId.equals(interaction.getProjectId()))
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
