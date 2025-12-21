package com.bteconosur.discord.util;

import java.util.List;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class MessageService {

    private static final ConsoleLogger logger = BTEConoSur.getConsoleLogger();

    @SuppressWarnings("null")
    public void sendMessage(TextChannel channel, String message) {
        if (!DiscordValidate.channel(channel) || !DiscordValidate.messageContent(message)) return;  
        try {
            channel.sendMessage(message).queue();
        } catch (Exception e) {
            logger.error("Discord: Error al enviar el mensaje al canal '" + channel.getName() + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public void sendEmbed(TextChannel channel, MessageEmbed embed) {
        if (!DiscordValidate.channel(channel) || !DiscordValidate.embed(embed)) return;
        try {
            channel.sendMessageEmbeds(embed).queue();
        } catch (Exception e) {
            logger.error("Discord: Error al enviar el embed al canal '" + channel.getName() + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public void sendReplyMessage(TextChannel channel, Message replyMessage, String message) {
        if (!DiscordValidate.channel(channel) || !DiscordValidate.replyMessage(replyMessage) || !DiscordValidate.messageContent(message)) return;
        try {
            channel.sendMessage(message).setMessageReference(replyMessage).queue();
        } catch (Exception e) {
            logger.error("Discord: Error al enviar el mensaje de respuesta al canal '" + channel.getName() + "': ", e);
        }
    }

    @SuppressWarnings("null")
    public void sendReplyEmbed(TextChannel channel, Message replyMessage, MessageEmbed embed) {
        if (!DiscordValidate.channel(channel) || !DiscordValidate.replyMessage(replyMessage) || !DiscordValidate.embed(embed)) return;
        try {
            channel.sendMessageEmbeds(embed).setMessageReference(replyMessage).queue();
        } catch (Exception e) {
            logger.error("Discord: Error al enviar el embed de respuesta al canal '" + channel.getName() + "': ", e);
        }
    }

    public void sendBroadcastMessage(List<TextChannel> channels, String message) {
        for (TextChannel channel : channels) sendMessage(channel, message);
    }

    public void sendBroadcastEmbed(List<TextChannel> channels, MessageEmbed embed) {
        for (TextChannel channel : channels) sendEmbed(channel, embed);
    }

}