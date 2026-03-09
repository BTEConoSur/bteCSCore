package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.PlaceholderUtils;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Servicio de chat global del servidor.
 * Gestiona la difusión de mensajes y notificaciones a todos los jugadores conectados
 * al chat global, tanto en Minecraft como en Discord.
 */
public class GlobalChatService {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    /**
     * Notifica a los jugadores del chat global que un jugador se unio.
     *
     * @param player jugador que se une al chat global.
     */
    public static void joinChat(Player player) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcChatJoined(player, onlinePlayer.getLanguage())));
        }

        if (config.getBoolean("discord-player-join-leave-chat")) broadcastEmbed(ChatUtil.getDsChatJoined(player));
    }

    /**
     * Notifica a los jugadores del chat global que un jugador salió.
     *
     * @param player jugador que sale del chat global.
     */
    public static void leaveChat(Player player) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcChatLeft(player, onlinePlayer.getLanguage())));
        }

        if (config.getBoolean("discord-player-join-leave-chat")) broadcastEmbed(ChatUtil.getDsChatLeft(player));
    }

    /**
     * Reenvía un mensaje de Discord al chat global de Minecraft y al resto de canales globales de Discord.
     *
     * @param player jugador vinculado al autor del mensaje.
     * @param message contenido del mensaje.
     * @param dsFrom país/canal de origen del mensaje.
     * @param dsFromId id del canal de origen para evitar rebote.
     * @param attachments adjuntos del mensaje original.
     * @param messageId id del mensaje original en Discord.
     */
    public static void broadcastDsChat(Player player, String message, Pais dsFrom, Long dsFromId, List<Attachment> attachments, String messageId) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            String mcMessage = message;
            for (Attachment attachment : attachments) {
                if (attachment.isImage()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.image");
                else if (attachment.isVideo()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.video");
                else if (attachment.isSpoiler()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.spoiler");
                else mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.file");
            }
            List<String> processedHover = new ArrayList<>();
            for (String line : LanguageHandler.getTextList(onlinePlayer.getLanguage(), "player-hover-chat")) {
                processedHover.add(PlaceholderUtils.replaceMC(line, onlinePlayer.getLanguage(), player));
            }
            String hover = String.join("\n", processedHover);
            TagResolver hoverResolver = TagResolverUtils.getHoverText("player", player.getNombrePublico(), hover);
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, mcMessage, onlinePlayer.getLanguage(), dsFrom), hoverResolver));
        }
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        ids.remove(dsFromId);
        String dsMessage = message;
        for (Attachment attachment : attachments) dsMessage += " " + attachment.getUrl();
        MessageService.sendBroadcastMessage(ids, ChatUtil.getDsFormatedMessage(player, dsMessage, Language.getDefault(), dsFrom), messageId);
    }

    /**
     * Reenvía un mensaje de Discord al chat global cuando no hay un jugador vinculado.
     *
     * @param username nombre del autor del mensaje.
     * @param message contenido del mensaje.
     * @param dsFrom país/canal de origen del mensaje.
     * @param dsFromId id del canal de origen para evitar rebote.
     * @param attachments adjuntos del mensaje original.
     * @param messageId id del mensaje original en Discord.
     */
    public static void broadcastDsChat(String username, String message, Pais dsFrom, Long dsFromId, List<Attachment> attachments, String messageId) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            String mcMessage = message;
            for (Attachment attachment : attachments) {
                if (attachment.isImage()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.image");
                else if (attachment.isVideo()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.video");
                else if (attachment.isSpoiler()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.spoiler");
                else mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.file");
            }
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(username, mcMessage, onlinePlayer.getLanguage(), dsFrom)));
        }
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        ids.remove(dsFromId);
        String dsMessage = message;
        for (Attachment attachment : attachments) dsMessage += " " + attachment.getUrl();
        MessageService.sendBroadcastMessage(ids, ChatUtil.getDsFormatedMessage(username, dsMessage, Language.getDefault(), dsFrom), messageId);
    }

    /**
     * Difunde un mensaje de Minecraft al chat global y a Discord.
     *
     * @param player jugador autor del mensaje.
     * @param message contenido del mensaje.
     */
    public static void broadcastMcChat(Player player, String message) {
        List<Player> globalChatPlayers = ChatService.getPlayersInGlobalChatList();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!globalChatPlayers.contains(onlinePlayer)) continue;
            List<String> processedHover = new ArrayList<>();
            for (String line : LanguageHandler.getTextList(onlinePlayer.getLanguage(), "player-hover-chat")) {
                processedHover.add(PlaceholderUtils.replaceMC(line, onlinePlayer.getLanguage(), player));
            }
            String hover = String.join("\n", processedHover);
            TagResolver hoverResolver = TagResolverUtils.getHoverText("player", player.getNombrePublico(), hover);
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, message, onlinePlayer.getLanguage()), hoverResolver));
        }
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastMessage(ids, ChatUtil.getDsFormatedMessage(player, message, Language.getDefault()));
    }

    /**
     * Envia un embed a todos los canales globales de Discord.
     *
     * @param embed embed a difundir.
     */
    public static void broadcastEmbed(MessageEmbed embed) {
        if (!config.getBoolean("discord-global-chat")) return;
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        MessageService.sendBroadcastEmbed(ids, embed);
    }

    /**
     * Difunde el mensaje de ingreso de un jugador nuevo al servidor.
     *
     * @param player jugador que ingreso por primera vez.
     */
    public static void broadcastNewPlayerJoinedServer(Player player) {
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcNewPlayerJoined(player, onlinePlayer.getLanguage())));
        }
        if (!config.getBoolean("discord-player-join-leave")) return;
        MessageEmbed dsMessage = ChatUtil.getDsNewPlayerJoined(player);
        broadcastEmbed(dsMessage);
    }

    /**
     * Difunde el mensaje de ingreso de un jugador al servidor.
     *
     * @param player jugador que ingreso al servidor.
     */
    public static void broadcastPlayerJoinedServer(Player player) {
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcPlayerJoined(player, onlinePlayer.getLanguage())));
        }
        if (!config.getBoolean("discord-player-join-leave")) return;
        MessageEmbed dsMessage = ChatUtil.getDsPlayerJoined(player);
        broadcastEmbed(dsMessage);
    }

    /**
     * Difunde el mensaje de salida de un jugador del servidor.
     *
     * @param player jugador que se desconectó.
     */
    public static void broadcastPlayerLeftServer(Player player) {
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcPlayerLeft(player, onlinePlayer.getLanguage())));
        }
        if (!config.getBoolean("discord-player-join-leave")) return;
        MessageEmbed dsMessage = ChatUtil.getDsPlayerLeft(player);
        broadcastEmbed(dsMessage);
    }

}
