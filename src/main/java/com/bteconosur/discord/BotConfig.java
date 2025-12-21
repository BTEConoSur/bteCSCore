package com.bteconosur.discord;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.discord.listener.ButtonListener;
import com.bteconosur.discord.listener.ModalListener;
import com.bteconosur.discord.listener.SelectListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotConfig {

    private final YamlConfiguration config;
    private final YamlConfiguration lang;
    private final ConsoleLogger logger;

    private JDA jda;

    public BotConfig() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        lang = configHandler.getLang();
        logger = BTEConoSur.getConsoleLogger();
    }

    public void startBot() {
        if (jda != null) {
            logger.warn("El Bot de Discord ya está iniciado. Usar restartBot() para reiniciarlo.");
        }
        
        logger.info(lang.getString("discord-bot-initializing"));
        try {
            jda = JDABuilder.createDefault(config.getString("discord-bot-token"))
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .setActivity(Activity.playing("Testando bot para Cono Sur"))
                    .addEventListeners(new ButtonListener(), new ModalListener(), new SelectListener())
                    .build().awaitReady();
        } catch (Exception e) {
            logger.error("Error al iniciar el Bot de Discord: " + e);
        }
    }

    public void stopBot() {
        logger.info(lang.getString("discord-bot-shutting-down"));
        if (jda == null) {
            logger.warn("El Bot de Discord no está iniciado.");
            return;
        }
        
        jda.shutdown();
        try {
            jda.awaitShutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Error al esperar el apagado del Bot de Discord: " + e);
        }
        jda = null;
        
    }

    public void restartBot() {
        logger.info(lang.getString("discord-bot-restarting"));
        stopBot();
        startBot();
    }

    public JDA getJDA() {
        if (jda == null) {
            logger.error("JDA no inicializado.");
        }
        return jda;
    }
}
