package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class CountryChatService {

    private static Map<Player, Pais> playersInChat = new HashMap<>();

    public static List<Player> getPlayersListInChat() {
        return new ArrayList<>(playersInChat.keySet());
    }

    public static Map<Player, Pais> getPlayersInChat() {
        return playersInChat;
    }

    public static void joinChat(Player player, Pais pais) {
        playersInChat.put(player, pais);
        broadcastMc(ChatUtil.getMcCountryChatJoined(player.getNombrePublico()), pais);

        sendEmbed(ChatUtil.getDsCountryChatJoined(player.getNombrePublico(), player.getUuid()), pais);
    }

    public static void leaveChat(Player player) {
        Pais pais = playersInChat.get(player);
        playersInChat.remove(player);
        broadcastMc(ChatUtil.getMcCountryChatLeft(player.getNombrePublico()), pais);

        sendEmbed(ChatUtil.getDsCountryChatLeft(player.getNombrePublico(), player.getUuid()), pais);
    }

    public static void sendChat(String dsMessage, String mcMessage, Pais pais) {
        broadcastMc(mcMessage, pais);
        if (!ConfigHandler.getInstance().getConfig().getBoolean("discord-country-chat")) return;
        MessageService.sendMessage(pais.getDsIdCountryChat(), dsMessage);
    }

    public static void sendChat(String mcMessage, Pais pais) {
        if (!ConfigHandler.getInstance().getConfig().getBoolean("discord-country-chat")) return;
        broadcastMc(mcMessage, pais);
    }

    public static void sendEmbed(MessageEmbed embed, Pais pais) {
        if (!ConfigHandler.getInstance().getConfig().getBoolean("discord-country-chat")) return;
        MessageService.sendEmbed(pais.getDsIdCountryChat(), embed);
    }

    public static void broadcastEmbed(MessageEmbed embed, String mcMessage) {
        broadcastMc(mcMessage);
        if (!ConfigHandler.getInstance().getConfig().getBoolean("discord-country-chat")) return;
        MessageService.sendBroadcastEmbed(PaisRegistry.getInstance().getDsCountryChatIds(), embed);
    }

    public static void broadcastEmbed(MessageEmbed embed) {
        if (!ConfigHandler.getInstance().getConfig().getBoolean("discord-country-chat")) return;
        MessageService.sendBroadcastEmbed(PaisRegistry.getInstance().getDsCountryChatIds(), embed);
    }

    private static void broadcastMc(String mcMessage, Pais pais) {
        for (Player player : Player.getOnlinePlayers()) {
            if (playersInChat.containsKey(player) && playersInChat.get(player).equals(pais)) {
                player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
            }
        }
    }

    private static void broadcastMc(String mcMessage) {
        for (Player player : Player.getOnlinePlayers()) {
            if (playersInChat.containsKey(player)) {
                player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
            }
        }
    }

}
