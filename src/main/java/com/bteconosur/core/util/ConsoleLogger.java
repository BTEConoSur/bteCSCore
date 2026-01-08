package com.bteconosur.core.util;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ConsoleLogger {

    private final BTEConoSur plugin;
    private final YamlConfiguration config;
    private final YamlConfiguration lang;

    // TODO: Hacerlo estatico

    private final ComponentLogger logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ConsoleLogger() {
        plugin = BTEConoSur.getInstance();
        logger = plugin.getComponentLogger();
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig(); 
        lang = configHandler.getLang();
    }

    public void info(String message) {
        logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("info-prefix") + message));
    }

    public void info(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("info-prefix") + message + "\n" + json));
    }

    public void debug(String message) {
        if (config.getBoolean("debug-mode", false)) {
            logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("debug-prefix") + message));
        }
    }

    public void debug(String message, Object object) {
        if (config.getBoolean("debug-mode", false)) {
            String json = JsonUtils.toJson(object);
            logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("debug-prefix") + message + "\n" + json));
        }
    }
    
    public void warn(String message) {
        logger.warn(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("warn-prefix") + message));
        DiscordLogger.staffConsoleLog(LoggerUtil.getWarnEmbed(message));
    }

    public void warn(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.warn(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("warn-prefix") + message + "\n" + json));
        DiscordLogger.staffConsoleLog(LoggerUtil.getWarnEmbed(message + "\n" + json));
    }

    public void error(String message) {
        logger.error(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("error-prefix") + message));
        DiscordLogger.staffConsoleLog(LoggerUtil.getErrorEmbed(message));
        DiscordLogger.notifyDevs(message);
    }

    public void error(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.error(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("error-prefix") + message + "\n" + json));
        DiscordLogger.staffConsoleLog(LoggerUtil.getErrorEmbed(message + "\n" + json));
        DiscordLogger.notifyDevs(message);
    }

    public void send(String message) {
        logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + message));
    }

    public void send(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + message + "\n" + json));
    }

}
