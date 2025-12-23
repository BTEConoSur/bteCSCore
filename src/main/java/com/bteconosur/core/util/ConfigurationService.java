package com.bteconosur.core.util;

import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.ConfigurationKey;

public class ConfigurationService {

    // 1- Crear configuración en la clase Configuración.
    // 2- Agregar valores por defecto en el config.yml.
    // 3- Agregar seteos de default según corresponda.
    // 4- Agregar configuración al enum ConfigurationKey.

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private static final ConsoleLogger logger = BTEConoSur.getConsoleLogger();

    public static void setDefaults(UUID uuid) {
        setGeneralDefaults(uuid);
        setReviewerDefaults(uuid);
        setManagerDefaults(uuid);
    }

    public static void setGeneralDefaults(UUID uuid) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(uuid).getConfiguration();

        configuration.setGeneralToggleTest(config.getBoolean("players.general.toggle-test"));

        playerRegistry.merge(uuid);
    }

    public static void setReviewerDefaults(UUID uuid) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(uuid).getConfiguration();

        configuration.setReviewerToggleTest(config.getBoolean("players.reviewer.toggle-test"));

        playerRegistry.merge(uuid);
    }

    public static void setManagerDefaults(UUID uuid) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(uuid).getConfiguration();

        configuration.setManagerToggleTest(config.getBoolean("players.manager.toggle-test"));

        playerRegistry.merge(uuid);
    }

    public static void toggle(UUID uuid, ConfigurationKey key) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(uuid).getConfiguration();

        switch (key) {
            case GENERAL_TOGGLE_TEST:
                configuration.toggleGeneralTest();
                break;
            case REVIEWER_TOGGLE_TEST:
                configuration.toggleReviewerTest();
                break;
            case MANAGER_TOGGLE_TEST:
                configuration.toggleManagerTest();
                break;
            default:
                logger.warn("Key no reconocida: " + key);
        }

        playerRegistry.merge(uuid);
    }

}
