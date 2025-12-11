package com.bteconosur.discord;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.JDA;

public class DiscordManager {

    private final YamlConfiguration lang;
    private final ConsoleLogger logger;

    private BotConfig botConfig;

    public DiscordManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        logger = BTEConoSur.getConsoleLogger();
      
        logger.info(lang.getString("discord-initializing"));

        botConfig = new BotConfig();
        botConfig.startBot();
    }

    public void restartBot() {
        botConfig.restartBot();
    }

    public JDA getJda() {
        return botConfig.getJDA();
    }

    public void shutdown() {
        logger.info(lang.getString("discord-shutting-down"));
        botConfig.stopBot();
    }
}
