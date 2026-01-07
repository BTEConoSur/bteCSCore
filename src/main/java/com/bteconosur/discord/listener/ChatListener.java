package com.bteconosur.discord.listener;

import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.chat.GlobalChatService;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.chat.CountryChatService;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChatListener extends ListenerAdapter {

    public static YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    @Override 
    public void onMessageReceived(MessageReceivedEvent event) {
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

        String dsMessage, mcMessage;
        if (player != null) {
            dsMessage = ChatUtil.getDsFormatedMessage(player, message, pais);
            mcMessage = ChatUtil.getMcFormatedMessage(player, message, pais);
        } else {
            String authorName = event.getAuthor().getName();
            dsMessage = ChatUtil.getDsFormatedMessage(authorName, message, pais);
            mcMessage = ChatUtil.getMcFormatedMessage(authorName, message, pais);
        }
        
        List<Attachment> attachments = event.getMessage().getAttachments();

        for (Attachment attachment : attachments) {
            if (attachment.isImage()) mcMessage += " " + lang.getString("mc-image");
            else if (attachment.isVideo()) mcMessage += " " + lang.getString("mc-video");
            else if (attachment.isSpoiler()) mcMessage += " " + lang.getString("mc-spoiler");
            else mcMessage += " " + lang.getString("mc-file");
            dsMessage += " " + attachment.getUrl();
        }

        if (isGlobalChat) {
            GlobalChatService.broadcastChat(dsMessage, mcMessage, channelId);
            return;
        }

        CountryChatService.sendChat(mcMessage, pais);
    }
}
