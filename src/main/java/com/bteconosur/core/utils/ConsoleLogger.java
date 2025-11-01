package com.bteconosur.core.utils;

import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.units.qual.s;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;

import net.kyori.adventure.text.Component;
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
        logger.info(miniMessage.deserialize(prefix + infoPrefix + message));
    }

    public void debug(String message) {
        if (debugMode) {
            logger.info(miniMessage.deserialize(prefix + debugPrefix + message));
        }
    }
    
    public void warn(String message) {
        logger.info(miniMessage.deserialize(prefix + warnPrefix + message));
    }

    public void error(String message) {
        logger.info(miniMessage.deserialize(prefix + errorPrefix + message));
    }


}
