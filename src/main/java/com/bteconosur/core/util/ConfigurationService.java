package com.bteconosur.core.util;

import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.ConfigurationKey;

public class ConfigurationService {

    // 1- Crear configuración en la clase Configuración.
    // 2- Agregar valores por defecto en el config.yml.
    // 3- Agregar el nombre y descripcion en lang.yml
    // 4- Agregar seteos de default según corresponda.
    // 5- Agregar configuración al enum ConfigurationKey.
    // 6- Añadir al menu de configuración.

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void setDefaults(UUID uuid) {
        setGeneralDefaults(uuid);
        setReviewerDefaults(uuid);
        setManagerDefaults(uuid);
    }

    public static void setGeneralDefaults(UUID uuid) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(uuid).getConfiguration();

        configuration.setGeneralGlobalChatOnJoin(config.getBoolean("player-defaults.general.global-chat-on-join"));

        playerRegistry.merge(uuid);
    }

    public static void setReviewerDefaults(UUID uuid) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(uuid).getConfiguration();

        configuration.setReviewerDsNotifications(config.getBoolean("player-defaults.reviewer.notifications"));

        playerRegistry.merge(uuid);
    }

    public static void setManagerDefaults(UUID uuid) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(uuid).getConfiguration();

        configuration.setManagerDsNotifications(config.getBoolean("player-defaults.manager.notifications"));

        playerRegistry.merge(uuid);
    }

    public static void toggle(UUID uuid, ConfigurationKey key) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(uuid).getConfiguration();

        switch (key) {
            case GENERAL_GLOBAL_CHAT_ON_JOIN:
                configuration.toggleGeneralGlobalChatOnJoin();
                break;
            case REVIEWER_DS_NOTIFICATIONS:
                configuration.toggleReviewerDsNotifications();
                break;
            case MANAGER_DS_NOTIFICATIONS:
                configuration.toggleManagerDsNotifications();
                break;
            default:
                ConsoleLogger.warn("Key no reconocida: " + key);
        }

        playerRegistry.merge(uuid);
    }

}
