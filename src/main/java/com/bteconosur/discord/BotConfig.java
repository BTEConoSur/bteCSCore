package com.bteconosur.discord;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.discord.listener.ButtonListener;
import com.bteconosur.discord.listener.ChatListener;
import com.bteconosur.discord.listener.ContextCommandListener;
import com.bteconosur.discord.listener.ModalListener;
import com.bteconosur.discord.listener.SelectListener;
import com.bteconosur.discord.listener.SlashCommandListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * Configuración y administración del bot de Discord.
 * Gestiona el ciclo de vida del bot: inicialización, apagado y reinicio.
 * Registra los listeners necesarios y configura los intents del bot.
 */
public class BotConfig {

    private final YamlConfiguration secret;

    private JDA jda;

    /**
     * Constructor de la configuración del bot.
     * Carga las configuraciones de los archivos YAML.
     */
    public BotConfig() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        secret = configHandler.getSecret();
    }

    /**
     * Inicia el bot de Discord.
     * Configura los intents, listeners y actividad del bot.
     */
    @SuppressWarnings("null")
    public void startBot() {
        if (jda != null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.bot-already-started"));
        }
        
        ConsoleLogger.info(LanguageHandler.getText("discord-bot-initializing"));
        try {
            jda = JDABuilder.createDefault(secret.getString("discord-bot-token"))
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setActivity(Activity.playing(LanguageHandler.getText("ds-activity").replace("%players%", String.valueOf(Bukkit.getOnlinePlayers().size()))))
                    .addEventListeners(new ButtonListener(), new ModalListener(), new SelectListener(), new SlashCommandListener(), new ChatListener(), new ContextCommandListener())
                    .build().awaitReady();
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.bot-initializing"), e);
        }
    }

    /**
     * Detiene el bot de Discord de forma segura.
     * Espera a que se complete el apagado antes de continuar.
     */
    public void stopBot() {
        ConsoleLogger.info(LanguageHandler.getText("discord-bot-shutting-down"));
        if (jda == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.bot-not-initialized"));
            return;
        }
        
        jda.shutdown();
        try {
            jda.awaitShutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ConsoleLogger.error(LanguageHandler.getText("ds-error.bot-shutting-down"), e);
        }
        jda = null;
        
    }

    /**
     * Reinicia el bot de Discord.
     * Detiene el bot actual y lo vuelve a iniciar.
     */
    public void restartBot() {
        ConsoleLogger.info(LanguageHandler.getText("discord-bot-restarting"));
        stopBot();
        startBot();
    }

    /**
     * Obtiene la instancia de JDA (Java Discord API).
     * 
     * @return La instancia de JDA o null si el bot no está inicializado
     */
    public JDA getJDA() {
        if (jda == null) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.bot-not-initialized"));
        }
        return jda;
    }
}
