package com.bteconosur.discord.listener;

import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.chat.GlobalChatService;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.chat.CountryChatService;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.IDUtils;
import com.bteconosur.discord.util.MessageRef;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChatListener extends ListenerAdapter {

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

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
            if (!config.getBoolean("discord-global-chat")) return;
            String idMessage = IDUtils.generarCodigoMessage();
            MessageService.addMessageKey(idMessage);
            MessageService.addMessageRef(idMessage, new MessageRef(channelId, event.getMessage().getIdLong()));
            if (player != null) GlobalChatService.broadcastDsChat(player, message, pais, channelId, attachments, idMessage);
            else GlobalChatService.broadcastDsChat(authorName, message, pais, channelId, attachments, idMessage);
            return;
        }

        if (!config.getBoolean("discord-country-chat")) return;
        if (player != null) CountryChatService.sendMcChat(player, message, pais, attachments);
        else CountryChatService.sendMcChat(authorName, message, pais, attachments);
        
    }
}
