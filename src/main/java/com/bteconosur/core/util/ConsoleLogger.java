package com.bteconosur.core.util;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ConsoleLogger {

    private final BTEConoSur plugin;
    private final ConfigHandler configHandler; 
    private final YamlConfiguration config;
    private final YamlConfiguration lang;

    private final ComponentLogger logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private Boolean debugMode;
    private String prefix;
    private String infoPrefix;
    private String debugPrefix;
    private String warnPrefix;
    private String errorPrefix;

    public ConsoleLogger() {
        plugin = BTEConoSur.getInstance();
        logger = plugin.getComponentLogger();
        configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig(); 
        lang = configHandler.getLang();

        debugMode = config.getBoolean("debug-mode", false);
        prefix = lang.getString("prefix");
        infoPrefix = lang.getString("info-prefix");
        debugPrefix = lang.getString("debug-prefix");
        warnPrefix = lang.getString("warn-prefix");
        errorPrefix = lang.getString("error-prefix");

        info(lang.getString("logger-loaded"));
        debug(lang.getString("debug-mode-enabled"));

    }

    public void info(String message) {
        logger.info(miniMessage.deserialize(prefix + " " + infoPrefix + message));
    }

    public void info(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.info(miniMessage.deserialize(prefix + " " + infoPrefix + message + "\n" + json));
    }

    public void debug(String message) {
        if (debugMode) {
            logger.info(miniMessage.deserialize(prefix + " " + debugPrefix + message));
        }
    }

    public void debug(String message, Object object) {
        if (debugMode) {
            String json = JsonUtils.toJson(object);
            logger.info(miniMessage.deserialize(prefix + " " + debugPrefix + message + "\n" + json));
        }
    }
    
    public void warn(String message) {
        logger.warn(miniMessage.deserialize(prefix + " " + warnPrefix + message));
    }

    public void warn(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.warn(miniMessage.deserialize(prefix + " " + warnPrefix + message + "\n" + json));
    }

    public void error(String message) {
        logger.error(miniMessage.deserialize(prefix + " " + errorPrefix + message));
    }

    public void error(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.error(miniMessage.deserialize(prefix + " " + errorPrefix + message + "\n" + json));
    }

    public void send(String message) {
        logger.info(miniMessage.deserialize(prefix + " " + message));
    }

    public void send(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.info(miniMessage.deserialize(prefix + " " + message + "\n" + json));
    }

}
