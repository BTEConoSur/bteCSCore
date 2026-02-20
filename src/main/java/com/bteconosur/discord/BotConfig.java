package com.bteconosur.discord;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
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

    private final YamlConfiguration secret;

    private JDA jda;

    public BotConfig() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        secret = configHandler.getSecret();
    }

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
                    .setActivity(Activity.playing("Testando bot para Cono Sur")) //TODO: Cambiar actividad del bot.
                    .addEventListeners(new ButtonListener(), new ModalListener(), new SelectListener(), new SlashCommandListener(), new ChatListener())
                    .build().awaitReady();
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.bot-initializing") + e);
        }
    }

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
            ConsoleLogger.error(LanguageHandler.getText("ds-error.bot-shutting-down") + e);
        }
        jda = null;
        
    }

    public void restartBot() {
        ConsoleLogger.info(LanguageHandler.getText("discord-bot-restarting"));
        stopBot();
        startBot();
    }

    public JDA getJDA() {
        if (jda == null) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.bot-not-initialized"));
        }
        return jda;
    }
}
