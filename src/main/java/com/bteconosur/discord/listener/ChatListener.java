package com.bteconosur.discord.listener;

import java.util.List;

import javax.annotation.Nonnull;

import com.bteconosur.core.chat.GlobalChatService;
import com.bteconosur.core.chat.CountryChatService;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChatListener extends ListenerAdapter {

    @Override 
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        PaisRegistry paisRegistry = PaisRegistry.getInstance();

        Pais pais = paisRegistry.findByDsGlobalChatId(event.getChannel().getIdLong());
        Boolean isGlobalChat = pais != null;
        if (pais == null) pais = paisRegistry.findByDsCountryChatId(event.getChannel().getIdLong());
        if (pais == null) return;

        Player player = playerRegistry.findByDiscordId(event.getAuthor().getIdLong());
        String message = event.getMessage().getContentDisplay();
        Long channelId = event.getChannel().getIdLong();

        String authorName = event.getAuthor().getName();     
        List<Attachment> attachments = event.getMessage().getAttachments();

        if (isGlobalChat) {
            if (player != null) GlobalChatService.broadcastDsChat(player, message, pais, channelId, attachments);
            else GlobalChatService.broadcastDsChat(authorName, message, pais, channelId, attachments);
            return;
        }

        if (player != null) CountryChatService.sendMcChat(player, message, pais, attachments);
        else CountryChatService.sendMcChat(authorName, message, pais, attachments);
    }
}
