package com.bteconosur.discord.listener;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ChatListener extends ListenerAdapter {

    public static YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    @Override 
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        PaisRegistry paisRegistry = PaisRegistry.getInstance();

        Pais pais = paisRegistry.findByDsGuildId(event.getGuild().getIdLong());
        if (pais == null) return;
        if (!paisRegistry.getDsGlobalChatIds().contains(event.getChannel().getIdLong())) return;

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

        for (Attachment attachment : event.getMessage().getAttachments()) {
            if (attachment.isImage()) {
                dsMessage += " " + attachment.getUrl();
                mcMessage += " " + lang.getString("mc-image");
            }
            else if (attachment.isVideo()) {
                dsMessage += " " + attachment.getUrl();
                mcMessage += " " + lang.getString("mc-video");
            }
        }

        ChatService.broadcastGlobalChat(dsMessage, mcMessage, channelId);
    }
}
