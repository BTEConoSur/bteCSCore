package com.bteconosur.core.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);
        Player player = PlayerRegistry.getInstance().get(event.getPlayer().getUniqueId());
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        ChatService.broadcastGlobalChat(ChatUtil.getDsFormatedMessage(player, message), ChatUtil.getMcFormatedMessage(player, message));
    }
}
