package com.bteconosur.core.chat;

import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class CountryChatService {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void joinChat(Player player, Pais pais) {
        broadcastMc(ChatUtil.getMcChatJoined(player.getNombrePublico()), pais);

        if (config.getBoolean("discord-player-join-leave-chat")) sendEmbed(ChatUtil.getDsChatJoined(player.getNombrePublico(), player.getUuid()), pais);
    }

    public static void leaveChat(Player player, Pais pais) {
        broadcastMc(ChatUtil.getMcChatLeft(player.getNombrePublico()), pais);

        if (config.getBoolean("discord-player-join-leave-chat")) sendEmbed(ChatUtil.getDsChatLeft(player.getNombrePublico(), player.getUuid()), pais);
    }

    public static void sendChat(String dsMessage, String mcMessage, Pais pais) {
        broadcastMc(mcMessage, pais);
        if (!config.getBoolean("discord-country-chat")) return;
        MessageService.sendMessage(pais.getDsIdCountryChat(), dsMessage);
    }

    public static void sendChat(String mcMessage, Pais pais) {
        if (!config.getBoolean("discord-country-chat")) return;
        broadcastMc(mcMessage, pais);
    }

    public static void sendEmbed(MessageEmbed embed, Pais pais) {
        if (!config.getBoolean("discord-country-chat")) return;
        MessageService.sendEmbed(pais.getDsIdCountryChat(), embed);
    }

    public static void broadcastEmbed(MessageEmbed embed, String mcMessage) {
        broadcastMc(mcMessage);
        if (!config.getBoolean("discord-country-chat")) return;
        MessageService.sendBroadcastEmbed(PaisRegistry.getInstance().getDsCountryChatIds(), embed);
    }

    public static void broadcastEmbed(MessageEmbed embed) {
        if (!config.getBoolean("discord-country-chat")) return;
        MessageService.sendBroadcastEmbed(PaisRegistry.getInstance().getDsCountryChatIds(), embed);
    }

    public static void broadcastMc(String mcMessage, Pais pais) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player player : Player.getOnlinePlayers()) {
            if (playersInChat.containsKey(player) && playersInChat.get(player).equals(pais)) {
                player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
            }
        }
    }

    public static void broadcastMc(String mcMessage) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player player : Player.getOnlinePlayers()) {
            if (!playersInChat.containsKey(player)) continue;
            player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
        }
    }

}
