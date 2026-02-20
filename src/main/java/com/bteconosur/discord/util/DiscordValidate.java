package com.bteconosur.discord.util;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public final class DiscordValidate {

    public static boolean jda() {
        if (BTEConoSur.getDiscordManager() == null || BTEConoSur.getDiscordManager().getJda() == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.not-jda"));
            return false;
        }
        return true;
    }   

    public static boolean channelId(Long channelId) {
        if (channelId == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-channel-id"));
            return false;
        }
        return true;
    }

    public static boolean userId(Long userId) {
        if (userId == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-user-id"));
            return false;
        }
        return true;
    }

    public static boolean user(User user) {
        if (user == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-user"));
            return false;
        }
        return true;
    }

    public static boolean messageContent(String message) {
        if (message == null || message.isEmpty()) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-message"));
            return false;
        }
        return true;
    }

    public static boolean channel(TextChannel channel) {
        if (channel == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-channel"));
            return false;
        }
        return true;
    }

    public static boolean messageId(Long messageId) {
        if (messageId == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-message-id"));
            return false;
        }
        return true;
    }

    public static boolean embed(MessageEmbed embed) {
        if (embed == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-embed"));
            return false;
        }
        return true;
    }
}
