package com.bteconosur.db.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.db.model.DiscordInteraction;
import com.bteconosur.db.util.InteractionKey;
import com.bteconosur.discord.action.ButtonAction;
import com.bteconosur.discord.action.ModalAction;
import com.bteconosur.discord.action.SelectAction;

public class DiscordInteractionRegistry extends Registry<Long, DiscordInteraction> {

    private static DiscordInteractionRegistry instance;

    private final Map<InteractionKey, ModalAction> modalActions = new HashMap<>();
    private final Map<InteractionKey, ButtonAction> buttonActions = new HashMap<>();
    private final Map<InteractionKey, SelectAction> selectActions = new HashMap<>();

    public DiscordInteractionRegistry() {
        super();
        logger.info(lang.getString("discord-interaction-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<DiscordInteraction> interactions = dbManager.selectAll(DiscordInteraction.class);
        if (interactions != null) {
            for (DiscordInteraction interaction : interactions) {
                loadedObjects.put(interaction.getId(), interaction);
            }
        }

        try {
            int expirationMinutes = config.getInt("discord-interaction-expiration");
            long periodTicks = 20L * 60L * expirationMinutes;
            BTEConoSur.getInstance().getServer().getScheduler().runTaskTimer(BTEConoSur.getInstance(), () -> {
                purgeExpired();
            }, periodTicks, periodTicks);
            logger.info("Tarea de purga de interacciones de Discord programada cada " + expirationMinutes + " minutos.");
        } catch (Exception e) {
            logger.warn("No se pudo programar la purga periódica de interacciones de Discord: " + e.getMessage());
        }
    }

    @Override
    public void load(DiscordInteraction obj) {
        if (obj == null || obj.getId() == null) return;
        loadedObjects.put(obj.getId(), obj);
        dbManager.save(obj);
    }

    @Override
    public void shutdown() {
        logger.info(lang.getString("discord-interaction-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public void purgeExpired() {
        loadedObjects.entrySet().removeIf(entry -> {
            DiscordInteraction interaction = entry.getValue();
            boolean expired = interaction.isExpired();
            if (expired) dbManager.remove(interaction);
            return expired;
        });
    }

    public void removeInteraction(DiscordInteraction interaction) {
        if (interaction == null || interaction.getId() == null) return;
        loadedObjects.remove(interaction.getId());
        dbManager.remove(interaction);
    }

    public DiscordInteraction findByComponentId(String componentId) {
        if (componentId == null) return null;
        for (DiscordInteraction interaction : loadedObjects.values()) {
            if (componentId.equals(interaction.getComponentId())) return interaction;
        }
        return null;
    }

    public DiscordInteraction findByMessageId(Long messageId) {
        if (messageId == null) return null;
        for (DiscordInteraction interaction : loadedObjects.values()) {
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

    public static DiscordInteractionRegistry getInstance() {
        if (instance == null) {
            instance = new DiscordInteractionRegistry();
        }
        return instance;
    }
}
