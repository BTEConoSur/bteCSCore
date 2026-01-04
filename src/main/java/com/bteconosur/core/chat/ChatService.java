package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ChatService {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void broadcastGlobalChat(String dsMessage, String mcMessage, Long dsFrom) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        ids.remove(dsFrom);
        MessageService.sendBroadcastMessage(ids, dsMessage);
    }

    public static void broadcastGlobalChat(String dsMessage, String mcMessage) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastMessage(ids, dsMessage);
    }

    public static void broadcastGlobalChatEmbed(MessageEmbed embed) {
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastEmbed(ids, embed);
    }

    public static void broadcastGlobalChatEmbed(MessageEmbed embed, String mcMessage, Long dsFrom) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        ids.remove(dsFrom);
        MessageService.sendBroadcastEmbed(ids, embed);
    }

    public static void broadcastGlobalChatEmbed(MessageEmbed embed, String mcMessage) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastEmbed(ids, embed);
    }

    private static void broadcastMc(String mcMessage) {
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
        }
    }
}
