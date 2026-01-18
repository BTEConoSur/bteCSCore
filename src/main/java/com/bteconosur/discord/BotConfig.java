package com.bteconosur.discord;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.discord.listener.ButtonListener;
import com.bteconosur.discord.listener.ChatListener;
import com.bteconosur.discord.listener.ModalListener;
import com.bteconosur.discord.listener.SelectListener;
import com.bteconosur.discord.listener.SlashCommandListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotConfig {

    private final YamlConfiguration config;
    private final YamlConfiguration lang;

    private JDA jda;

    public BotConfig() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        lang = configHandler.getLang();
    }

    public void startBot() {
        if (jda != null) {
            ConsoleLogger.warn("El Bot de Discord ya está iniciado. Usar restartBot() para reiniciarlo.");
        }
        
        ConsoleLogger.info(lang.getString("discord-bot-initializing"));
        try {
            jda = JDABuilder.createDefault(config.getString("discord-bot-token"))
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setActivity(Activity.playing("Testando bot para Cono Sur"))
                    .addEventListeners(new ButtonListener(), new ModalListener(), new SelectListener(), new SlashCommandListener(), new ChatListener())
                    .build().awaitReady();
        } catch (Exception e) {
            ConsoleLogger.error("Error al iniciar el Bot de Discord: " + e);
        }
    }

    public void stopBot() {
        ConsoleLogger.info(lang.getString("discord-bot-shutting-down"));
        if (jda == null) {
            ConsoleLogger.warn("El Bot de Discord no está iniciado.");
            return;
        }
        
        jda.shutdown();
        try {
            jda.awaitShutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ConsoleLogger.error("Error al esperar el apagado del Bot de Discord: " + e);
        }
        jda = null;
        
    }

    public void restartBot() {
        ConsoleLogger.info(lang.getString("discord-bot-restarting"));
        stopBot();
        startBot();
    }

    public JDA getJDA() {
        if (jda == null) {
            ConsoleLogger.error("JDA no inicializado.");
        }
        
        return jda;
    }
}
