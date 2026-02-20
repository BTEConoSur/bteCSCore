package com.bteconosur.core.chat;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class CountryChatService {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void joinChat(Player player, Pais pais) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!playersInChat.containsKey(onlinePlayer)) continue;
            if (playersInChat.get(onlinePlayer).equals(pais)) {
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcChatJoined(player, onlinePlayer.getLanguage())));
            }
        }

        if (config.getBoolean("discord-player-join-leave-chat")) sendEmbed(ChatUtil.getDsChatJoined(player), pais);
    }

    public static void leaveChat(Player player, Pais pais) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!playersInChat.containsKey(onlinePlayer)) continue;
            if (playersInChat.get(onlinePlayer).equals(pais)) {
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcChatLeft(player, onlinePlayer.getLanguage())));
            }
        }

        if (config.getBoolean("discord-player-join-leave-chat")) sendEmbed(ChatUtil.getDsChatLeft(player), pais);
    }

    public static void sendBothChat(Player player, String message, Pais pais) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (playersInChat.containsKey(onlinePlayer) && playersInChat.get(onlinePlayer).equals(pais)) {
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, message, onlinePlayer.getLanguage())));
            }
        }

        if (!config.getBoolean("discord-country-chat")) return;
        MessageService.sendMessage(pais.getDsIdCountryChat(), ChatUtil.getDsFormatedMessage(player, message, Language.getDefault()));
    }

    public static void sendMcChat(Player player, String message, Pais pais, List<Attachment> attachments) {
        if (!config.getBoolean("discord-country-chat")) return;
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (playersInChat.containsKey(onlinePlayer) && playersInChat.get(onlinePlayer).equals(pais)) {
                String mcMessage = message;
                for (Attachment attachment : attachments) {
                    if (attachment.isImage()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.image");
                    else if (attachment.isVideo()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.video");
                    else if (attachment.isSpoiler()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.spoiler");
                    else mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.file");
                }
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, mcMessage, onlinePlayer.getLanguage())));
            }
        }
    }

    public static void sendMcChat(String username, String message, Pais pais, List<Attachment> attachments) {
        if (!config.getBoolean("discord-country-chat")) return;
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (playersInChat.containsKey(onlinePlayer) && playersInChat.get(onlinePlayer).equals(pais)) {
                String mcMessage = message;
                for (Attachment attachment : attachments) {
                    if (attachment.isImage()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.image");
                    else if (attachment.isVideo()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.video");
                    else if (attachment.isSpoiler()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.spoiler");
                    else mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.file");
                }
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(username, mcMessage, onlinePlayer.getLanguage(), pais)));
            }
        }
    }

    public static void sendEmbed(MessageEmbed embed, Pais pais) {
        if (!config.getBoolean("discord-country-chat")) return;
        MessageService.sendEmbed(pais.getDsIdCountryChat(), embed);
    }

}
