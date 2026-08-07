package com.bteconosur.core.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.bteconosur.core.chat.GlobalChatService;
import com.bteconosur.core.chat.NotePadService;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.chat.CountryChatService;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Preset;
import com.bteconosur.db.registry.PlayerRegistry;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();

        if (!msg.startsWith("/")) return;

        String[] args = msg.split(" ");
        if (args.length >= 1) {
            args[0] = args[0].toLowerCase();
        }

        msg = String.join(" ", args);

        // Presets
        Player commandPlayer = PlayerRegistry.getInstance().get(event.getPlayer().getUniqueId());
        if (commandPlayer == null) return; 

        String presetSymbol = ConfigHandler.getInstance().getConfig().getString("preset.symbol");
        String quotedSymbol = Pattern.quote(presetSymbol);
        
        Pattern pattern = Pattern.compile(quotedSymbol + "([a-zA-Z0-9_]{1,30})" + quotedSymbol);
        Matcher matcher = pattern.matcher(msg);

        while (matcher.find()) {
            String presetName = matcher.group(1);

            Preset preset = commandPlayer.getPreset(presetName);
            if (preset == null) continue; 

            msg = msg.replace(matcher.group(), preset.getBlocks());
        }

        event.setMessage(msg);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);
        Player player = PlayerRegistry.getInstance().get(event.getPlayer().getUniqueId());
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        //message = message.replaceAll(".*<[^>]+>.*", "");
        
        if (ChatService.isInCountryChat(player)) {
            if (ChatService.isMuted(player.getUuid())) return;
            Pais pais = ChatService.getCountry(player);
            CountryChatService.sendBothChat(player, message, pais);
            return;
        }

        if (ChatService.isInGlobalChat(player)) {
            if (ChatService.isMuted(player.getUuid())) return;
            GlobalChatService.broadcastMcChat(player, message);
            return;
        }

        if (ChatService.isInNotePad(player)) {
            NotePadService.sendChat(player, message);
            return;
        }
    }
}
