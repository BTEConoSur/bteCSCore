package com.bteconosur.discord;

import org.bukkit.Bukkit;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

/**
 * Gestor principal de Discord del plugin.
 * Administra la configuración del bot, actualiza la actividad y proporciona
 * acceso a la instancia de JDA para interactuar con Discord.
 */
public class DiscordManager {

    public static DiscordManager instance;

    private BotConfig botConfig;

    /**
     * Constructor del gestor de Discord.
     * Inicializa la configuración del bot y lo inicia.
     */
    public DiscordManager() {
        ConsoleLogger.info(LanguageHandler.getText("discord-initializing"));

        botConfig = new BotConfig();
        botConfig.startBot();
    }

    /**
     * Reinicia el bot de Discord.
     */
    public void restartBot() {
        botConfig.restartBot();
    }

    /**
     * Obtiene la instancia de JDA para interactuar con Discord.
     * 
     * @return La instancia de JDA
     */
    public JDA getJda() {
        return botConfig.getJDA();
    }

    /**
     * Actualiza la actividad del bot mostrando el número de jugadores en línea.
     * 
     * @param leave Si es true, resta 1 al contador de jugadores (para cuando un jugador se desconecta)
     */
    @SuppressWarnings("null")
    public void updateActivity(boolean leave) {
        if (getJda() != null) {
            int playerCount = Bukkit.getOnlinePlayers().size();
            if (leave) {
                playerCount--;
            }
            getJda().getPresence().setActivity(Activity.playing(LanguageHandler.getText("ds-activity").replace("%players%", String.valueOf(playerCount))));
        }
    }

    /**
     * Obtiene la instancia singleton del gestor de Discord.
     * 
     * @return La instancia única de DiscordManager
     */
    public static DiscordManager getInstance() {
        if (instance == null) {
            instance = new DiscordManager();
        }
        return instance;
    }

    /**
     * Cierra el gestor de Discord y detiene el bot.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("discord-shutting-down"));

        botConfig.stopBot();
    }
}
