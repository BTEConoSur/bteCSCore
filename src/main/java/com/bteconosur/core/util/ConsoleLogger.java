package com.bteconosur.core.util;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.json.JsonUtils;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ConsoleLogger {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    private static final ComponentLogger logger = BTEConoSur.getInstance().getComponentLogger();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void info(String message) {
        logger.info(miniMessage.deserialize((LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("info-prefix") + message)));
    }

    public static void info(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.info(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("info-prefix") + message + "\n" + json));
    }

    public static void debug(String message) {
        if (config.getBoolean("debug-mode", false)) {
            logger.info(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("debug-prefix") + message));
        }
    }

    public static void debug(String message, Object object) {
        if (config.getBoolean("debug-mode", false)) {
            String json = JsonUtils.toJson(object);
            logger.info(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("debug-prefix") + message + "\n" + json));
        }
    }

    public static void warn(String message) {
        logger.warn(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("warn-prefix") + message));
        DiscordLogger.staffConsoleLog(LoggerUtil.getWarnEmbed(message));
    }

    public static void warn(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.warn(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("warn-prefix") + message + "\n" + json));
        DiscordLogger.staffConsoleLog(LoggerUtil.getWarnEmbed(message + "\n" + json));
    }

    public static void error(String message) {
        logger.error(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("error-prefix") + message));
        DiscordLogger.staffConsoleLog(LoggerUtil.getErrorEmbed(message));
        DiscordLogger.notifyDevs(message);
    }

    public static void error(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.error(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("error-prefix") + message + "\n" + json));
        DiscordLogger.staffConsoleLog(LoggerUtil.getErrorEmbed(message + "\n" + json));
        DiscordLogger.notifyDevs(message);
    }

    public static void error(String message, Object object, Throwable throwable) {
        String json = JsonUtils.toJson(object);
        String errorInfo = getCompactStackTrace(throwable);
        logger.error(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("error-prefix") + message + "\n" + json + "\n" + errorInfo));
        DiscordLogger.staffConsoleLog(LoggerUtil.getErrorEmbed(message + "\n" + json));
        DiscordLogger.notifyDevs(errorInfo);
        saveStackTraceToFile(message, throwable);
    }

    public static void error(String message, Throwable throwable) {
        String errorInfo = getCompactStackTrace(throwable);
        logger.error(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + LanguageHandler.getText("error-prefix") + message + "\n" + errorInfo));
        DiscordLogger.staffConsoleLog(LoggerUtil.getErrorEmbed(message));
        DiscordLogger.notifyDevs(errorInfo);
        saveStackTraceToFile(message, throwable);
    }   

    private static String getCompactStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getSimpleName()).append(" - ").append(throwable.getMessage());

        StackTraceElement[] trace = throwable.getStackTrace();
        int limit = Math.min(3, trace.length);
        for (int i = 0; i < limit; i++) {
            sb.append("\n  at ").append(trace[i]);
        }
        if (trace.length > 3) {
            sb.append("\n  ... ").append(trace.length - 3).append(" more");
        }

        return sb.toString();
    }

    private static void saveStackTraceToFile(String message, Throwable throwable) {
        try {
            File logsDir = new File(BTEConoSur.getInstance().getDataFolder(), "logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String timestamp = LocalDateTime.now().format(formatter);
            String fileName = timestamp + ".log";
            File logFile = new File(logsDir, fileName);

            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write("=== ERROR LOG ===\n");
                writer.write("Timestamp: " + LocalDateTime.now() + "\n");
                writer.write("Message: " + message + "\n");
                writer.write("Exception: " + throwable.getClass().getName() + "\n");
                writer.write("Reason: " + throwable.getMessage() + "\n");
                writer.write("\nStacktrace:\n");

                for (StackTraceElement element : throwable.getStackTrace()) {
                    writer.write("  at " + element + "\n");
                }

                writer.write("\n=== END LOG ===\n");
            }
        } catch (Exception e) {
            logger.warn("No se pudo guardar el stacktrace en archivo: " + e.getMessage());
        }
    }

    public static void send(String message) {
        logger.info(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + message));
    }

    public static void send(String message, Object object) {
        String json = JsonUtils.toJson(object);
        logger.info(miniMessage.deserialize(LanguageHandler.getText("prefix") + " " + message + "\n" + json));
    }

}
