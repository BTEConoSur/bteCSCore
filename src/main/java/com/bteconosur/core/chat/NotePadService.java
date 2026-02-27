package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.List;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.util.PlaceholderUtils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class NotePadService {

    public static void sendChat(Player player, String message) {
        List<String> processedHover = new ArrayList<>();
        for (String line : LanguageHandler.getTextList(player.getLanguage(), "player-hover-chat")) {
            processedHover.add(PlaceholderUtils.replaceMC(line, player.getLanguage(), player));
        }
        String hover = String.join("\n", processedHover);
        TagResolver hoverResolver = TagResolverUtils.getHoverText("player", player.getNombrePublico(), hover);
        player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, message, player.getLanguage()), hoverResolver));
    }
}
