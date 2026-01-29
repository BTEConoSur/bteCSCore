package com.bteconosur.discord.util;

import java.util.List;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class MessageService {

    @SuppressWarnings("null")
    public static void sendMessage(Long channelId, String message) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channelId(channelId) || !DiscordValidate.messageContent(message)) return;
        sendMessage(BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId), message);
    }

    @SuppressWarnings("null")
    public static void sendMessage(TextChannel channel, String message) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channel(channel) || !DiscordValidate.messageContent(message)) return;  
        try {
            channel.sendMessage(message).queue();
        } catch (Exception e) {
            ConsoleLogger.error("Discord: Error al enviar el mensaje al canal '" + channel.getName() + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public static void sendDM(Long dsUserId, String message) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.userId(dsUserId) || !DiscordValidate.messageContent(message)) return;
        BTEConoSur.getDiscordManager().getJda().retrieveUserById(dsUserId).queue(user -> sendDM(user, message));
    }

    @SuppressWarnings("null")
    public static void sendDM(User user, String message) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.user(user) || !DiscordValidate.messageContent(message)) return;

        try {
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
        } catch (Exception e) {
            ConsoleLogger.error("Discord: Error al enviar el mensaje al usuario '" + user.getIdLong() + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public static void sendEmbed(Long channelId, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channelId(channelId) || !DiscordValidate.embed(embed)) return;
        sendEmbed(BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId), embed);
    }

    @SuppressWarnings("null")
    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channel(channel) || !DiscordValidate.embed(embed)) return;
        try {
            channel.sendMessageEmbeds(embed).queue();
        } catch (Exception e) {
            ConsoleLogger.error("Discord: Error al enviar el embed al canal '" + channel.getName() + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public static void sendEmbedDM(Long dsUserId, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.userId(dsUserId) || !DiscordValidate.embed(embed)) return;
        BTEConoSur.getDiscordManager().getJda().retrieveUserById(dsUserId).queue(user -> sendEmbedDM(user, embed));
    }

    @SuppressWarnings("null")
    public static void sendEmbedDM(User user, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.user(user) || !DiscordValidate.embed(embed)) return;
        try {
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(embed).queue());
        } catch (Exception e) {
            ConsoleLogger.error("Discord: Error al enviar el mensaje al usuario '" + user.getIdLong() + "': ", e);
        }
    }

    public static void sendBroadcastMessage(List<Long> channelsIds, String message) {
        if (!DiscordValidate.jda()) return;
        for (Long channelId : channelsIds) {
            if (!DiscordValidate.channelId(channelId)) continue;
            TextChannel channel = getTextChannelById(channelId);
            sendMessage(channel, message);
        }
    }

    public static void sendBroadcastEmbed(List<Long> channelsIds, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        for (Long channelId : channelsIds) {
            if (!DiscordValidate.channelId(channelId)) continue;
            TextChannel channel = getTextChannelById(channelId);
            sendEmbed(channel, embed);
        }
    }

    public static void sendBroadcastDM(List<Long> usersIds, String message) {
        if (!DiscordValidate.jda()) return;
        for (Long userId : usersIds) {
            if (!DiscordValidate.userId(userId)) continue;
            sendDM(userId, message);
        }
    }

    public static void sendBroadcastEmbedDM(List<Long> usersIds, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        for (Long userId : usersIds) {
            if (!DiscordValidate.userId(userId)) continue;
            sendEmbedDM(userId, embed);
        }
    }
    
    public static void deleteMessage(Long channelId, Long messageId) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channelId(channelId) || !DiscordValidate.messageId(messageId)) return;
        TextChannel channel = BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId);
        if (channel == null) return;
        try {
            channel.deleteMessageById(messageId).queue();
        } catch (Exception e) {
            ConsoleLogger.error("Discord: Error al eliminar el mensaje '" + messageId + "' del canal '" + channel.getName() + "': ", e);
        }
    }

    public static TextChannel getTextChannelById(Long channelId) {
        if (!DiscordValidate.jda()) return null;
        if (!DiscordValidate.channelId(channelId)) return null;
        return BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId);
    }

}