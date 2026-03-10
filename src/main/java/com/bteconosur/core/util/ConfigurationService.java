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

/**
 * Servicio de gestión de configuraciones de jugador.
 * Establece valores por defecto para nuevos jugadores y guarda cambios
 * en la configuración individual de cada uno.
 */
public class ConfigurationService {

    // 1- Crear configuración en la clase Configuración.
    // 2- Agregar valores por defecto en el config.yml.
    // 3- Agregar el nombre y descripcion en lang.yml y en gui.yml los materiales. 
    // 4- Agregar seteos de default según corresponda.
    // 5- Agregar configuración al enum ConfigurationKey.
    // 6- Añadir al menú de configuración correspondiente.

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    /**
     * Establece todas las configuraciones por defecto para un jugador nuevo.
     *
     * @param player jugador para el que se establecen los defaults.
     * @return jugador actualizado con configuraciones por defecto.
     */
    public static Player setDefaults(Player player) {
        player = setGeneralDefaults(player);
        player = setReviewerDefaults(player);
        player = setManagerDefaults(player);
        return player;
    }

    /**
     * Establece configuraciones generales por defecto para un jugador.
     *
     * @param player jugador a configurar.
     * @return jugador actualizado con configuraciones generales por defecto.
     */
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

        configuration.setScoreboardOnline(config.getBoolean("player-defaults.scoreboard.online"));
        configuration.setScoreboardPlayer(config.getBoolean("player-defaults.scoreboard.player"));
        configuration.setScoreboardProyecto(config.getBoolean("player-defaults.scoreboard.proyecto"));

        return playerRegistry.merge(player.getUuid());
    }

    /**
     * Establece configuraciones de reviewer por defecto para un jugador.
     *
     * @param player jugador a configurar.
     * @return jugador actualizado con configuraciones de reviewer por defecto.
     */
    public static Player setReviewerDefaults(Player player) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();

        configuration.setReviewerDsNotifications(config.getBoolean("player-defaults.reviewer.ds-notifications"));

        return playerRegistry.merge(player.getUuid());
    }

    /**
     * Establece configuraciones de manager por defecto para un jugador.
     *
     * @param player jugador a configurar.
     * @return jugador actualizado con configuraciones de manager por defecto.
     */
    public static Player setManagerDefaults(Player player) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();

        configuration.setManagerDsNotifications(config.getBoolean("player-defaults.manager.ds-notifications"));

        return playerRegistry.merge(player.getUuid());
    }

    /**
     * Cambia el idioma de configuración de un jugador.
     *
     * @param player jugador a actualizar.
     * @param language nuevo idioma.
     * @return jugador con idioma actualizado.
     */
    public static Player setLang(Player player, Language language) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Configuration configuration = playerRegistry.get(player.getUuid()).getConfiguration();
        configuration.setLang(language);
        return playerRegistry.merge(player.getUuid());
    }

    /**
     * Guarda cambios en las configuraciones especificadas para un jugador.
     *
     * @param player jugador cuya configuración se guarda.
     * @param keys conjunto de claves de configuración a actualizar.
     * @return jugador con configuraciones guardadas.
     */
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
                case SCOREBOARD_ONLINE:
                    configuration.toggleScoreboardOnline();
                    break;
                case SCOREBOARD_PLAYER:
                    configuration.toggleScoreboardPlayer(); 
                    break;
                case SCOREBOARD_PROYECTO:
                    configuration.toggleScoreboardProyecto();
                    break;
                default:
                    ConsoleLogger.warn(LanguageHandler.getText("config-key-error").replace("%key%", key.name()));
            }
        }

        return playerRegistry.merge(player.getUuid());
    }

}
