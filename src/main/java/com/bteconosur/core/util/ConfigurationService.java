package com.bteconosur.core.util;

import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.scoreboard.ScoreboardManager;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.ConfigurationKey;

public class ConfigurationService {

    // 1- Crear configuración en la clase Configuración.
    // 2- Agregar valores por defecto en el config.yml.
    // 3- Agregar el nombre y descripcion en lang.yml y en gui.yml los materiales. 
    // 4- Agregar seteos de default según corresponda.
    // 5- Agregar configuración al enum ConfigurationKey.
    // 6- Añadir al menú de configuración correspondiente.

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
        configuration.setGeneralLabelBorder(config.getBoolean("player-defaults.general.label-border"));
        configuration.setGeneralProjectTitle(config.getBoolean("player-defaults.general.project-title"));
        configuration.setGeneralDivisionTitle(config.getBoolean("player-defaults.general.division-title"));
        configuration.setGeneralScoreboard(config.getBoolean("player-defaults.general.scoreboard"));
        configuration.setLang(Language.SPANISH);

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

    public static Player setLang(Player player, Language language) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();
        configuration.setLang(language);
        return playerRegistry.merge(player.getUuid());
    }

    public static Player save(Player player, Set<ConfigurationKey> keys) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();
        for (ConfigurationKey key : keys) {
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
                case GENERAL_LABEL_BORDER:
                    configuration.toggleGeneralLabelBorder();
                    break;
                case GENERAL_PROJECT_TITLE:
                    configuration.toggleGeneralProjectTitle();
                    break;
                case GENERAL_DIVISION_TITLE:
                    configuration.toggleGeneralDivisionTitle();
                    break;
                case GENERAL_SCOREBOARD:
                    configuration.toggleGeneralScoreboard();
                    if (configuration.getGeneralScoreboard()) {
                        ScoreboardManager.getInstance().addPlayer(player);
                    } else {
                        ScoreboardManager.getInstance().removePlayer(player);
                    }
                    break;
                case REVIEWER_DS_NOTIFICATIONS:
                    configuration.toggleReviewerDsNotifications();
                    break;
                case MANAGER_DS_NOTIFICATIONS:
                    configuration.toggleManagerDsNotifications();
                    break;
                default:
                    ConsoleLogger.warn(LanguageHandler.getText("config-key-error").replace("%key%", key.name()));
            }
        }

        return playerRegistry.merge(player.getUuid());
    }

}
