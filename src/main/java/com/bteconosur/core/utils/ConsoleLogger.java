package com.bteconosur.core.utils;

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
        log(prefix + " " + infoPrefix + message);
    }

    public void info(String message, Object object) {
        String json = JsonUtils.toJson(object);
        log(prefix + " " + infoPrefix + message + "\n" + json);
    }

    public void debug(String message) {
        if (debugMode) {
            log(prefix + " " + debugPrefix + message);
        }
    }

    public void debug(String message, Object object) {
        if (debugMode) {
            String json = JsonUtils.toJson(object);
            log(prefix + " " + debugPrefix + message + "\n" + json);
        }
    }
    
    public void warn(String message) {
        log(prefix + " " + warnPrefix + message);
    }

    public void warn(String message, Object object) {
        String json = JsonUtils.toJson(object);
        log(prefix + " " + warnPrefix + message + "\n" + json);
    }

    public void error(String message) {
        log(prefix + " " + errorPrefix + message);
    }

    public void error(String message, Object object) {
        String json = JsonUtils.toJson(object);
        log(prefix + " " + errorPrefix + message + "\n" + json);
    }

    public void send(String message) {
        log(prefix + " " + message);
    }

    public void send(String message, Object object) {
        String json = JsonUtils.toJson(object);
        log(prefix + " " + message + "\n" + json);
    }

    private void log(String message) {
        logger.info(miniMessage.deserialize(message));
    }

}
