package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GlobalChatService {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void joinChat(Player player) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcChatJoined(player, onlinePlayer.getLanguage())));
        }

        if (config.getBoolean("discord-player-join-leave-chat")) broadcastEmbed(ChatUtil.getDsChatJoined(player));
    }

    public static void leaveChat(Player player) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcChatLeft(player, onlinePlayer.getLanguage())));
        }

        if (config.getBoolean("discord-player-join-leave-chat")) broadcastEmbed(ChatUtil.getDsChatLeft(player));
    }

    public static void broadcastDsChat(Player player, String message, Pais dsFrom, Long dsFromId, List<Attachment> attachments) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            String mcMessage = message;
            for (Attachment attachment : attachments) {
                if (attachment.isImage()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.image");
                else if (attachment.isVideo()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.video");
                else if (attachment.isSpoiler()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.spoiler");
                else mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.file");
            }
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, mcMessage, onlinePlayer.getLanguage(), dsFrom)));
        }
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        ids.remove(dsFromId);
        String dsMessage = message;
        for (Attachment attachment : attachments) dsMessage += " " + attachment.getUrl();
        MessageService.sendBroadcastMessage(ids, ChatUtil.getDsFormatedMessage(player, dsMessage, Language.getDefault(), dsFrom));
    }

    public static void broadcastDsChat(String username, String message, Pais dsFrom, Long dsFromId, List<Attachment> attachments) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            String mcMessage = message;
            for (Attachment attachment : attachments) {
                if (attachment.isImage()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.image");
                else if (attachment.isVideo()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.video");
                else if (attachment.isSpoiler()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.spoiler");
                else mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.file");
            }
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(username, mcMessage, onlinePlayer.getLanguage(), dsFrom)));
        }
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        ids.remove(dsFromId);
        String dsMessage = message;
        for (Attachment attachment : attachments) dsMessage += " " + attachment.getUrl();
        MessageService.sendBroadcastMessage(ids, ChatUtil.getDsFormatedMessage(username, dsMessage, Language.getDefault(), dsFrom));
    }

    public static void broadcastMcChat(Player player, String message) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, message, onlinePlayer.getLanguage())));
        }
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastMessage(ids, ChatUtil.getDsFormatedMessage(player, message, Language.getDefault()));
    }

    public static void broadcastEmbed(MessageEmbed embed) {
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastEmbed(ids, embed);
    }

    public static void broadcastPlayerJoinedServer(Player player) {
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcPlayerJoined(player, onlinePlayer.getLanguage())));
        }
        if (!config.getBoolean("discord-player-join-leave")) return;
        MessageEmbed dsMessage = ChatUtil.getDsPlayerJoined(player);
        broadcastEmbed(dsMessage);
    }

    public static void broadcastPlayerLeftServer(Player player) {
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcPlayerLeft(player, onlinePlayer.getLanguage())));
        }
        if (!config.getBoolean("discord-player-join-leave")) return;
        MessageEmbed dsMessage = ChatUtil.getDsPlayerLeft(player);
        broadcastEmbed(dsMessage);
    }

}
