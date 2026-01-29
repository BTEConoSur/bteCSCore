package com.bteconosur.discord.util;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public final class DiscordValidate {

    public static boolean jda() {
        if (BTEConoSur.getDiscordManager() == null || BTEConoSur.getDiscordManager().getJda() == null) {
            ConsoleLogger.warn("Discord: Acción salteada. El bot de Discord no está inicializado.");
            return false;
        }
        return true;
    }   

    public static boolean channelId(Long channelId) {
        if (channelId == null) {
            ConsoleLogger.warn("Discord: El ID del canal es nulo o vacío.");
            return false;
        }
        return true;
    }

    public static boolean userId(Long userId) {
        if (userId == null) {
            ConsoleLogger.warn("Discord: El ID del usuario es nulo o vacío.");
            return false;
        }
        return true;
    }

    public static boolean user(User user) {
        if (user == null) {
            ConsoleLogger.warn("Discord: El usuario es nulo.");
            return false;
        }
        return true;
    }

    public static boolean messageContent(String message) {
        if (message == null || message.isEmpty()) {
            ConsoleLogger.warn("Discord: El mensaje es nulo o vacío.");
            return false;
        }
        return true;
    }

    public static boolean channel(TextChannel channel) {
        if (channel == null) {
            ConsoleLogger.warn("Discord: El canal es nulo.");
            return false;
        }
        return true;
    }

    public static boolean messageId(Long messageId) {
        if (messageId == null) {
            ConsoleLogger.warn("Discord: El ID del mensaje es nulo o vacío.");
            return false;
        }
        return true;
    }

    public static boolean replyMessage(Message replyMessage) {
        if (replyMessage == null) {
            ConsoleLogger.warn("Discord: El mensaje de respuesta es nulo.");
            return false;
        }
        return true;
    }

    public static boolean replyMessageId(Long replyMessageId) {
        if (replyMessageId == null) {
            ConsoleLogger.warn("Discord: El ID del mensaje de respuesta es nulo o vacío.");
            return false;
        }
        return true;
    }

    public static boolean embed(MessageEmbed embed) {
        if (embed == null) {
            ConsoleLogger.warn("Discord: El embed es nulo.");
            return false;
        }
        return true;
    }
}
