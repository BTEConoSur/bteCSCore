package com.bteconosur.discord;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.JDA;

public class DiscordManager {

    public static DiscordManager instance;

    private BotConfig botConfig;

    public DiscordManager() {
        ConsoleLogger.info(LanguageHandler.getText("discord-initializing"));

        botConfig = new BotConfig();
        botConfig.startBot();
    }

    public void restartBot() {
        botConfig.restartBot();
    }

    public JDA getJda() {
        return botConfig.getJDA();
    }

    public static DiscordManager getInstance() {
        if (instance == null) {
            instance = new DiscordManager();
        }
        return instance;
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("discord-shutting-down"));

        botConfig.stopBot();
    }
}
