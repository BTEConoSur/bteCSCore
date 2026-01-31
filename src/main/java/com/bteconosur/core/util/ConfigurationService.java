package com.bteconosur.core.util;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.ConfigurationKey;

public class ConfigurationService {

    // 1- Crear configuración en la clase Configuración.
    // 2- Agregar valores por defecto en el config.yml.
    // 3- Agregar el nombre y descripcion en lang.yml
    // 4- Agregar seteos de default según corresponda.
    // 5- Agregar configuración al enum ConfigurationKey.
    // 6- Añadir al menú de configuración correspondiente.


    // TODO: cachear config y poner boton para guardar cambios
    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static Player setDefaults(Player player) {
        player = setGeneralDefaults(player);
        player = setReviewerDefaults(player);
        player = setManagerDefaults(player);
        return player;
    }

    public static Player setGeneralDefaults(Player player) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();

        configuration.setGeneralGlobalChatOnJoin(config.getBoolean("player-defaults.general.global-chat-on-join"));
        configuration.setGeneralSimultaneousNotifications(config.getBoolean("player-defaults.general.simultaneous-notifications"));
        configuration.setGeneralPaisBorder(config.getBoolean("player-defaults.general.pais-border"));

        return playerRegistry.merge(player.getUuid());
    }

    public static Player setReviewerDefaults(Player player) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();

        configuration.setReviewerDsNotifications(config.getBoolean("player-defaults.reviewer.ds-notifications"));

        return playerRegistry.merge(player.getUuid());
    }

    public static Player setManagerDefaults(Player player) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();

        configuration.setManagerDsNotifications(config.getBoolean("player-defaults.manager.ds-notifications"));

        return playerRegistry.merge(player.getUuid());
    }

    public static Player toggle(Player player, ConfigurationKey key) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();

        switch (key) {
            case GENERAL_GLOBAL_CHAT_ON_JOIN:
                configuration.toggleGeneralGlobalChatOnJoin();
                break;
            case GENERAL_SIMULTANEOUS_NOTIFICATIONS:
                configuration.toggleGeneralSimultaneousNotifications();
                break;
            case GENERAL_PAIS_BORDER:
                configuration.toggleGeneralPaisBorder();
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

        return playerRegistry.merge(player.getUuid());
    }

}
