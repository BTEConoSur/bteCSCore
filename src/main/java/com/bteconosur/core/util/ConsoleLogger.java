package com.bteconosur.core.util;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ConsoleLogger {

    private static final ConfigHandler configHandler = ConfigHandler.getInstance();
    private static final YamlConfiguration config = configHandler.getConfig();
    private static final YamlConfiguration lang = configHandler.getLang();

    private static final ComponentLogger logger = BTEConoSur.getInstance().getComponentLogger();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void info(String message) {
        logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("info-prefix") + message));
    }

    public static void info(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("info-prefix") + message + "\n" + json));
    }

    public static void debug(String message) {
        if (config.getBoolean("debug-mode", false)) {
            logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("debug-prefix") + message));
        }
    }

    public static void debug(String message, Object object) {
        if (config.getBoolean("debug-mode", false)) {
            String json = JsonUtils.toJson(object);
            logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("debug-prefix") + message + "\n" + json));
        }
    }

    public static void warn(String message) {
        logger.warn(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("warn-prefix") + message));
        DiscordLogger.staffConsoleLog(LoggerUtil.getWarnEmbed(message));
    }

    public static void warn(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.warn(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("warn-prefix") + message + "\n" + json));
        DiscordLogger.staffConsoleLog(LoggerUtil.getWarnEmbed(message + "\n" + json));
    }

    public static void error(String message) {
        logger.error(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("error-prefix") + message));
        DiscordLogger.staffConsoleLog(LoggerUtil.getErrorEmbed(message));
        DiscordLogger.notifyDevs(message);
    }

    public static void error(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.error(miniMessage.deserialize(lang.getString("prefix") + " " + lang.getString("error-prefix") + message + "\n" + json));
        DiscordLogger.staffConsoleLog(LoggerUtil.getErrorEmbed(message + "\n" + json));
        DiscordLogger.notifyDevs(message);
    }

    public static void send(String message) {
        logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + message));
    }

    public static void send(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.info(miniMessage.deserialize(lang.getString("prefix") + " " + message + "\n" + json));
    }

}
