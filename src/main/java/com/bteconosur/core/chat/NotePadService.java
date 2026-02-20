package com.bteconosur.core.chat;

import com.bteconosur.db.model.Player;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class NotePadService {

    public static void sendChat(Player player, String message) {
        player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, message, player.getLanguage())));
    }
}
