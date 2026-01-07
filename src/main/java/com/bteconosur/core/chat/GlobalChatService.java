package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GlobalChatService {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void joinChat(Player player) {
        broadcastMc(ChatUtil.getMcChatJoined(player.getNombrePublico()));

        if (config.getBoolean("discord-player-join-leave-chat")) broadcastEmbed(ChatUtil.getDsChatJoined(player.getNombrePublico(), player.getUuid()));
    }

    public static void leaveChat(Player player) {
        broadcastMc(ChatUtil.getMcChatLeft(player.getNombrePublico()));

        if (config.getBoolean("discord-player-join-leave-chat")) broadcastEmbed(ChatUtil.getDsChatLeft(player.getNombrePublico(), player.getUuid()));
    }

    public static void broadcastChat(String dsMessage, String mcMessage, Long dsFrom) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        ids.remove(dsFrom);
        MessageService.sendBroadcastMessage(ids, dsMessage);
    }

    public static void broadcastChat(String dsMessage, String mcMessage) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastMessage(ids, dsMessage);
    }

    public static void broadcastEmbed(MessageEmbed embed) {
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastEmbed(ids, embed);
    }

    public static void broadcastEmbed(MessageEmbed embed, String mcMessage, Long dsFrom) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        ids.remove(dsFrom);
        MessageService.sendBroadcastEmbed(ids, embed);
    }

    public static void broadcastEmbed(MessageEmbed embed, String mcMessage) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastEmbed(ids, embed);
    }

    public static void broadcastMc(String mcMessage) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(player)) continue;
            player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
        }
    }

}
