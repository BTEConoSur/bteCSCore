package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.PlaceholderUtils;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Servicio de chat por país.
 * Gestiona el envío de mensajes y notificaciones en los canales de chat específicos de cada país,
 * tanto en Minecraft como en Discord.
 */
public class CountryChatService {

    private static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    /**
     * Notifica a los miembros del chat de país que un jugador se une.
     *
     * @param player jugador que se une al chat.
     * @param pais país del chat al que se une.
     */
    public static void joinChat(Player player, Pais pais) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!playersInChat.containsKey(onlinePlayer)) continue;
            if (playersInChat.get(onlinePlayer).equals(pais)) {
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcChatJoined(player, onlinePlayer.getLanguage())));
            }
        }

        if (config.getBoolean("discord-player-join-leave-chat")) sendEmbed(ChatUtil.getDsChatJoined(player), pais);
    }

    /**
     * Notifica a los miembros del chat de país que un jugador sale.
     *
     * @param player jugador que sale del chat.
     * @param pais país del chat que abandona.
     */
    public static void leaveChat(Player player, Pais pais) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!playersInChat.containsKey(onlinePlayer)) continue;
            if (playersInChat.get(onlinePlayer).equals(pais)) {
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcChatLeft(player, onlinePlayer.getLanguage())));
            }
        }

        if (config.getBoolean("discord-player-join-leave-chat")) sendEmbed(ChatUtil.getDsChatLeft(player), pais);
    }

    /**
     * Envía un mensaje de chat de país tanto a Minecraft como a Discord.
     *
     * @param player jugador autor del mensaje.
     * @param message contenido del mensaje.
     * @param pais país del chat donde se envia.
     */
    public static void sendBothChat(Player player, String message, Pais pais) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (playersInChat.containsKey(onlinePlayer) && playersInChat.get(onlinePlayer).equals(pais)) {
                List<String> processedHover = new ArrayList<>();
                for (String line : LanguageHandler.getTextList(onlinePlayer.getLanguage(), "player-hover-chat")) {
                    processedHover.add(PlaceholderUtils.replaceMC(line, onlinePlayer.getLanguage(), player));
                }
                String hover = String.join("\n", processedHover);
                TagResolver hoverResolver = TagResolverUtils.getHoverText("player", player.getNombrePublico(), hover);
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, message, onlinePlayer.getLanguage()), hoverResolver));
            }
        }

        if (!config.getBoolean("discord-country-chat")) return;
        MessageService.sendMessage(pais.getDsIdCountryChat(), ChatUtil.getDsFormatedMessage(player, message, Language.getDefault()));
    }

    /**
     * Envía a Minecraft un mensaje proveniente de Discord para el chat de país.
     *
     * @param player jugador vinculado al autor de Discord.
     * @param message contenido del mensaje.
     * @param pais país del chat destino.
     * @param attachments adjuntos recibidos junto al mensaje.
     */
    public static void sendMcChat(Player player, String message, Pais pais, List<Attachment> attachments) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (playersInChat.containsKey(onlinePlayer) && playersInChat.get(onlinePlayer).equals(pais)) {
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
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(player, mcMessage, onlinePlayer.getLanguage(), pais), hoverResolver));
            }
        }
    }

    /**
     * Envía a Minecraft un mensaje proveniente de un usuario no registrado en Discord para el chat de país.
     *
     * @param username nombre del autor.
     * @param message contenido del mensaje.
     * @param pais país del chat destino.
     * @param attachments adjuntos recibidos junto al mensaje.
     */
    public static void sendMcChat(String username, String message, Pais pais, List<Attachment> attachments) {
        Map<Player, Pais> playersInChat = ChatService.getPlayersInCountryChat();
        for (Player onlinePlayer : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (playersInChat.containsKey(onlinePlayer) && playersInChat.get(onlinePlayer).equals(pais)) {
                String mcMessage = message;
                for (Attachment attachment : attachments) {
                    if (attachment.isImage()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.image");
                    else if (attachment.isVideo()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.video");
                    else if (attachment.isSpoiler()) mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.spoiler");
                    else mcMessage += " " + LanguageHandler.getText(onlinePlayer.getLanguage(), "placeholder.chat-mc.file");
                }
                onlinePlayer.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(ChatUtil.getMcFormatedMessage(username, mcMessage, onlinePlayer.getLanguage(), pais)));
            }
        }
    }

    /**
     * Envía un embed al canal de Discord asociado al chat de país.
     *
     * @param embed embed a enviar.
     * @param pais país del chat destino.
     */
    public static void sendEmbed(MessageEmbed embed, Pais pais) {
        if (!config.getBoolean("discord-country-chat")) return;
        MessageService.sendEmbed(pais.getDsIdCountryChat(), embed);
    }

}
