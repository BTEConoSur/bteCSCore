package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;

public class ChatService {

    private static List<Player> playersInGlobalChat = new ArrayList<>();
    private static Map<Player, Pais> playersInCountryChat = new HashMap<>();
    private static Map<Player, Pais> playersLastCountryChat = new HashMap<>();

    public static void switchChatToGlobal(Player player) {
        if (playersInGlobalChat.contains(player)) return;

        if (playersInCountryChat.containsKey(player)) {
            Pais pais = playersInCountryChat.get(player);
            playersInCountryChat.remove(player);
            playersLastCountryChat.remove(player);
            CountryChatService.leaveChat(player, pais);
        }

        //TODO: Notificacion al jugador
        GlobalChatService.joinChat(player);
        playersInGlobalChat.add(player);
    }

    public static void setChatToGlobal(Player player) {
        if (playersInGlobalChat.contains(player)) return;

        if (playersLastCountryChat.containsKey(player)) {
            playersLastCountryChat.remove(player);
            GlobalChatService.joinChat(player);
        }

        playersInGlobalChat.add(player);
    }

    public static void setCountryChat(Player player) {
        Pais pais = playersLastCountryChat.get(player);
        if (playersInCountryChat.containsKey(player) && playersInCountryChat.get(player).equals(pais)) return;

        if (playersInGlobalChat.contains(player)) playersInGlobalChat.remove(player);
        GlobalChatService.leaveChat(player);
        CountryChatService.joinChat(player, pais);
        playersInCountryChat.put(player, pais);
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

        //TODO: Notificacion al jugador
        CountryChatService.joinChat(player, pais);
        playersInCountryChat.put(player, pais);
        playersLastCountryChat.put(player, pais);
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

    public static boolean isInCountryChat(Player player, Pais pais) {
        return playersInCountryChat.containsKey(player) && playersInCountryChat.get(player).equals(pais);
    }

    public static Pais getCountry(Player player) {
        return playersInCountryChat.get(player);
    }

    public static List<Player> getPlayersInGlobalChatList() {
        return playersInGlobalChat;
    }

    public static Map<Player, Pais> getPlayersInCountryChat() {
        return playersInCountryChat;
    }

    public static List<Player> getPlayersInCountryChatList() {
        return new ArrayList<>(playersInCountryChat.keySet());
    }

}
