package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;

public class ChatService {

    private static List<Player> playersInGlobalChat = new ArrayList<>();

    private static Map<Player, Pais> playersInCountryChat = new HashMap<>();
    private static Map<Player, Pais> playersLastCountryChat = new HashMap<>();

    private static List<Player> playersInNotePad = new ArrayList<>();

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public static void switchChatToGlobal(Player player) {
        if (playersInGlobalChat.contains(player)) return;

        if (playersInCountryChat.containsKey(player)) {
            Pais pais = playersInCountryChat.get(player);
            playersInCountryChat.remove(player);
            playersLastCountryChat.remove(player);
            CountryChatService.leaveChat(player, pais);
        }

        if (playersInNotePad.contains(player)) {
            playersInNotePad.remove(player);
        }

        PlayerLogger.info(player, lang.getString("global-chat-switched"), (String) null);
        GlobalChatService.joinChat(player);
        playersInGlobalChat.add(player);
    }

    public static void setChatToGlobal(Player player) {
        if (playersInGlobalChat.contains(player)) return;

        if (playersLastCountryChat.containsKey(player)) {
            playersLastCountryChat.remove(player);
            GlobalChatService.joinChat(player);
        }

        if (playersInNotePad.contains(player)) {
            playersInNotePad.remove(player);
            GlobalChatService.joinChat(player);
        }

        playersInGlobalChat.add(player);
    }

    public static void setChatToCountry(Player player) {
        Pais pais = playersLastCountryChat.get(player);
        if (playersInCountryChat.containsKey(player) && playersInCountryChat.get(player).equals(pais)) return;

        if (playersInGlobalChat.contains(player)) playersInGlobalChat.remove(player);
        GlobalChatService.leaveChat(player);
        CountryChatService.joinChat(player, pais);
        playersInCountryChat.put(player, pais);
    }

    public static void setChatToNotePad(Player player) {
        GlobalChatService.leaveChat(player);
        PlayerLogger.info(player, lang.getString("note-pad-chat-switched"), (String) null);
    }

    public static void switchChatToCountry(Player player, Pais pais) {
        if (playersInCountryChat.containsKey(player) && playersInCountryChat.get(player).equals(pais)) return;

        if (playersInGlobalChat.contains(player)) {
            playersInGlobalChat.remove(player);
            GlobalChatService.leaveChat(player);
        }

        if (playersInCountryChat.containsKey(player)) {
            Pais previousPais = playersInCountryChat.get(player);
            playersInCountryChat.remove(player);
            CountryChatService.leaveChat(player, previousPais);
        }

        if (playersInNotePad.contains(player)) {
            playersInNotePad.remove(player);
        }

        PlayerLogger.info(player, lang.getString("country-chat-switched").replace("%pais%", pais.getNombrePublico()), (String) null);
        CountryChatService.joinChat(player, pais);
        playersInCountryChat.put(player, pais);
        playersLastCountryChat.put(player, pais);
    }

    public static void switchChatToNotePad(Player player) {
        if (playersInNotePad.contains(player)) return;

        if (playersInGlobalChat.contains(player)) {
            playersInGlobalChat.remove(player);
            GlobalChatService.leaveChat(player);
        }

        if (playersInCountryChat.containsKey(player)) {
            Pais pais = playersInCountryChat.get(player);
            playersInCountryChat.remove(player);
            CountryChatService.leaveChat(player, pais);
        }

        PlayerLogger.info(player, lang.getString("notepad-switched"), (String) null);
        playersInNotePad.add(player);
    }

    public static void leaveChat(Player player) {
        if (playersInGlobalChat.contains(player)) {
            playersInGlobalChat.remove(player);
            return;
        }

        if (playersInCountryChat.containsKey(player)) {
            Pais pais = playersInCountryChat.get(player);
            playersInCountryChat.remove(player);
            CountryChatService.leaveChat(player, pais);
        }
    }

    public static boolean isInGlobalChat(Player player) {
        return playersInGlobalChat.contains(player);
    }

    public static boolean isInCountryChat(Player player) {
        return playersInCountryChat.containsKey(player);
    }

    public static boolean wasInCountryChat(Player player) {
        return playersLastCountryChat.containsKey(player);
    }

    public static boolean isInNotePad(Player player) {
        return playersInNotePad.contains(player);
    }

    public static boolean isInCountryChat(Player player, Pais pais) {
        return playersInCountryChat.containsKey(player) && playersInCountryChat.get(player).equals(pais);
    }

    public static Pais getCountry(Player player) {
        return playersInCountryChat.get(player);
    }

    public static List<Player> getPlayersInGlobalChatList() {
        return playersInGlobalChat;
    }

    public static List<Player> getPlayersInNotePadList() {
        return playersInNotePad;
    }

    public static Map<Player, Pais> getPlayersInCountryChat() {
        return playersInCountryChat;
    }

    public static List<Player> getPlayersInCountryChatList() {
        return new ArrayList<>(playersInCountryChat.keySet());
    }

}
