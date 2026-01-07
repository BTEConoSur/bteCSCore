package com.bteconosur.core.chat;

import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class NotePadService {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void sendChat(String mcMessage, Player player) {
        if (!config.getBoolean("discord-country-chat")) return;
        player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
    }

    public static void broadcastMc(String mcMessage) {
        List<Player> notePadPlayers = ChatService.getPlayersInNotePadList();
        for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!notePadPlayers.contains(player)) continue;
            player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
        }
    }
}
