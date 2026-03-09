package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.util.PlaceholderUtils.PlaceholderContext;

/**
 * Servicio centralizado de gestión de chats del servidor.
 * Administra los estados de chat de los jugadores (global, país, bloc de notas)
 * y provee métodos para cambiar entre modos de chat.
 */
public class ChatService {

    private static List<Player> playersInGlobalChat = new ArrayList<>();

    private static Map<Player, Pais> playersInCountryChat = new HashMap<>();
    private static Map<Player, Pais> playersLastCountryChat = new HashMap<>();

    private static List<Player> playersInNotePad = new ArrayList<>();

    /**
     * Obtiene el texto del placeholder que indica el chat actual del jugador.
     *
     * @param player jugador a consultar.
     * @param context contexto de dónde se usa el placeholder (Minecraft o Discord).
     * @param language idioma en el que se devuelve el texto.
     * @return texto del chat actual o {@code ERROR_NO_CHAT} si el jugador no está en ningun chat.
     */
    public static String getChat(Player player, PlaceholderContext context, Language language) {
        String path;
        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.chat-mc.";
        else path = "placeholder.chat-ds.";
        if (playersInGlobalChat.contains(player)) return LanguageHandler.getText(language, path + "global");
        if (playersInCountryChat.containsKey(player)) return LanguageHandler.replaceMC(path + "country", language, playersInCountryChat.get(player));
        if (playersInNotePad.contains(player)) return LanguageHandler.getText(language, path + "notepad");
        return LanguageHandler.getText(language, path + "global");
    }

    /**
     * Cambia el chat del jugador a chat global y notifica el cambio al jugador.
     *
     * @param player jugador a actualizar.
     */
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

        PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "global-chat.switched"), (String) null);
        GlobalChatService.joinChat(player);
        playersInGlobalChat.add(player);
    }

    /**
     * Cambia el chat del jugador a chat global sin notificar el cambio al jugador.
     *
     * @param player jugador a actualizar.
     */
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

    /**
     * Cambia el chat del jugador al último chat de país usado sin notificar el cambio al jugador.
     *
     * @param player jugador a actualizar.
     */
    public static void setChatToCountry(Player player) {
        Pais pais = playersLastCountryChat.get(player);
        if (playersInCountryChat.containsKey(player) && playersInCountryChat.get(player).equals(pais)) return;

        if (playersInGlobalChat.contains(player)) playersInGlobalChat.remove(player);
        GlobalChatService.leaveChat(player);
        CountryChatService.joinChat(player, pais);
        playersInCountryChat.put(player, pais);
    }

    /**
     * Cambia el chat del jugador al modo bloc de notas.
     *
     * @param player jugador a actualizar.
     */
    public static void setChatToNotePad(Player player) {
        GlobalChatService.leaveChat(player);
        PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "notepad.switched"), (String) null);
    }

    /**
     * Cambia el chat del jugador al de un pais especifico, guarda ese pais como último usado y notifica el cambio.
     *
     * @param player a actualizar.
     * @param pais país destino del chat.
     */
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

        String message = LanguageHandler.replaceMC("country-chat.switched", player.getLanguage(), pais);
        PlayerLogger.info(player, message, (String) null);
        CountryChatService.joinChat(player, pais);
        playersInCountryChat.put(player, pais);
        playersLastCountryChat.put(player, pais);
    }

    /**
     * Cambia el chat activo del jugador al bloc de notas.
     *
     * @param player a actualizar.
     */
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

        PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "notepad.switched"), (String) null);
        playersInNotePad.add(player);
    }

    /**
     * Saca al jugador del chat actual.
     *
     * @param player jugador que abandona el chat.
     */
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

    /**
     * Indica si el jugador está en chat global.
     *
     * @param player jugador a validar.
     * @return {@code true} si está en chat global, {@code false} en caso contrario.
     */
    public static boolean isInGlobalChat(Player player) {
        return playersInGlobalChat.contains(player);
    }

    /**
     * Indica si el jugador está en chat de país.
     *
     * @param player jugador a validar.
     * @return {@code true} si está en chat de país, {@code false} en caso contrario.
     */
    public static boolean isInCountryChat(Player player) {
        return playersInCountryChat.containsKey(player);
    }

    /**
     * Indica si el jugador tuvo un ultimo chat de país registrado.
     *
     * @param player jugador a validar.
     * @return {@code true} si tiene un país previo registrado, {@code false} en caso contrario.
     */
    public static boolean wasInCountryChat(Player player) {
        return playersLastCountryChat.containsKey(player);
    }

    /**
     * Indica si el jugador está en modo bloc de notas.
     *
     * @param player jugador a validar.
     * @return {@code true} si está en bloc de notas, {@code false} en caso contrario.
     */
    public static boolean isInNotePad(Player player) {
        return playersInNotePad.contains(player);
    }

    /**
     * Indica si el jugador está en el chat de un país especifico.
     *
     * @param player jugador a validar.
     * @param pais país que se compara.
     * @return {@code true} si el jugador está en ese país, {@code false} en caso contrario.
     */
    public static boolean isInCountryChat(Player player, Pais pais) {
        return playersInCountryChat.containsKey(player) && playersInCountryChat.get(player).equals(pais);
    }

    /**
     * Obtiene el país del chat activo del jugador.
     *
     * @param player jugador a consultar.
     * @return país del chat activo o {@code null} si no está en chat de país.
     */
    public static Pais getCountry(Player player) {
        return playersInCountryChat.get(player);
    }

    /**
     * Obtiene la lista interna de jugadores en chat global.
     *
     * @return lista mutable con jugadores en chat global.
     */
    public static List<Player> getPlayersInGlobalChatList() {
        return playersInGlobalChat;
    }

    /**
     * Obtiene la lista interna de jugadores en bloc de notas.
     *
     * @return lista mutable con jugadores en bloc de notas.
     */
    public static List<Player> getPlayersInNotePadList() {
        return playersInNotePad;
    }

    /**
     * Obtiene el mapa interno de jugadores y su chat de país.
     *
     * @return mapa mutable jugador-país del chat de país.
     */
    public static Map<Player, Pais> getPlayersInCountryChat() {
        return playersInCountryChat;
    }

    /**
     * Obtiene una copia de los jugadores que están en chat de país.
     *
     * @return lista nueva con los jugadores en chat de país.
     */
    public static List<Player> getPlayersInCountryChatList() {
        return new ArrayList<>(playersInCountryChat.keySet());
    }

}
