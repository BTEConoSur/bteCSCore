package com.bteconosur.discord.listener;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChatListener extends ListenerAdapter {

    @Override 
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        PaisRegistry paisRegistry = PaisRegistry.getInstance();

        Pais pais = paisRegistry.findByDsGuildId(event.getGuild().getIdLong());
        if (pais == null) return;
        if (!paisRegistry.getDsGlobalChatIds().contains(event.getChannel().getIdLong())) return;

        Player player = playerRegistry.findByDiscordId(event.getAuthor().getIdLong());
        
        if (player != null) ChatService.broadcastMessage(
            ChatService.getDsFormatedMessage(player, event.getMessage().getContentDisplay(), pais),
            ChatService.getMcFormatedMessage(player, event.getMessage().getContentDisplay(), pais),
            event.getChannel().getIdLong()
        );
        else ChatService.broadcastMessage(
            ChatService.getDsFormatedMessage(event.getAuthor().getName(), event.getMessage().getContentDisplay(), pais),
            ChatService.getMcFormatedMessage(event.getAuthor().getName(), event.getMessage().getContentDisplay(), pais),
            event.getChannel().getIdLong()
        );
    }
}
