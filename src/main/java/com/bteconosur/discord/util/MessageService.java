package com.bteconosur.discord.util;

import java.util.List;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class MessageService {

    private static final ConsoleLogger logger = BTEConoSur.getConsoleLogger();

    @SuppressWarnings("null")
    public static void sendMessage(TextChannel channel, String message) {
        if (!DiscordValidate.channel(channel) || !DiscordValidate.messageContent(message)) return;  
        try {
            channel.sendMessage(message).queue();
        } catch (Exception e) {
            logger.error("Discord: Error al enviar el mensaje al canal '" + channel.getName() + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public static void sendMessage(Long channelId, String message) {
        if (!DiscordValidate.channelId(channelId) || !DiscordValidate.messageContent(message)) return;

        try {
            TextChannel channel = BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId);
            channel.sendMessage(message).queue();
        } catch (Exception e) {
            logger.error("Discord: Error al enviar el mensaje al canal '" + channelId + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
        if (!DiscordValidate.channel(channel) || !DiscordValidate.embed(embed)) return;
        try {
            channel.sendMessageEmbeds(embed).queue();
        } catch (Exception e) {
            logger.error("Discord: Error al enviar el embed al canal '" + channel.getName() + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public static void sendEmbed(Long channelId, MessageEmbed embed) {
        if (!DiscordValidate.channelId(channelId) || !DiscordValidate.embed(embed)) return;
        try {
            TextChannel channel = BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId);
            channel.sendMessageEmbeds(embed).queue();
        } catch (Exception e) {
            logger.error("Discord: Error al enviar el embed al canal '" + channelId + "': ", e);
        }
    }

    public static void sendBroadcastMessage(List<Long> channelsIds, String message) {
        for (Long channelId : channelsIds) {
            if (!DiscordValidate.channelId(channelId)) continue;
            TextChannel channel = BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId);
            sendMessage(channel, message);
        }
    }

    public static void sendBroadcastEmbed(List<Long> channelsIds, MessageEmbed embed) {
        for (Long channelId : channelsIds) {
            if (!DiscordValidate.channelId(channelId)) continue;
            TextChannel channel = BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId);
            sendEmbed(channel, embed);
        }
    }

}