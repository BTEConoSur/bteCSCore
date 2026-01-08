package com.bteconosur.core.chat;

import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ChatUtil {

    private static YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public static String getMcFormatedMessage(Player player, String message) {
        String formatedMessage = lang.getString("mc-message");

        Pais pais = player.getPaisPrefix();
        if (pais != null) formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("mc-prefixes.pais." + pais.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("mc-prefixes.pais.internacional"));

        RangoUsuario rango = player.getRangoUsuario();
        if (rango != null) formatedMessage = formatedMessage.replace("%rangoPrefix%", lang.getString("mc-prefixes.rango." + rango.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%rangoPrefix%", "");

        TipoUsuario tipo = player.getTipoUsuario();
        if (tipo != null) formatedMessage = formatedMessage.replace("%tipoPrefix%", lang.getString("mc-prefixes.tipo." + tipo.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        formatedMessage = formatedMessage.replace("%player%", player.getNombrePublico());
        formatedMessage = formatedMessage.replace("%message%", message);

        return formatedMessage;
    }

    public static String getMcFormatedMessage(String username, String message, Pais dsPais) {
        String formatedMessage = lang.getString("from-ds-message");

        String dsPrefix = lang.getString("mc-prefixes.ds");
        dsPrefix = dsPrefix.replace("%pais%", dsPais.getNombrePublico());

        formatedMessage = formatedMessage.replace("%dsPrefix%", dsPrefix);
        formatedMessage = formatedMessage.replace("%player%", username);
        formatedMessage = formatedMessage.replace("%message%", message);

        formatedMessage = formatedMessage.replace("%paisPrefix%", "");
        formatedMessage = formatedMessage.replace("%rangoPrefix%", "");
        formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        return formatedMessage;
    }

    public static String getMcFormatedMessage(Player player, String message, Pais dsPais) {
        String formatedMessage = lang.getString("from-ds-message");

        String dsPrefix = lang.getString("mc-prefixes.ds");
        dsPrefix = dsPrefix.replace("%pais%", dsPais.getNombrePublico());

        formatedMessage = formatedMessage.replace("%dsPrefix%", dsPrefix);

        Pais pais = player.getPaisPrefix();
        if (pais != null) formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("mc-prefixes.pais." + pais.getNombre()));
        else formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("mc-prefixes.pais.internacional"));

        RangoUsuario rango = player.getRangoUsuario();
        if (rango != null) formatedMessage = formatedMessage.replace("%rangoPrefix%", lang.getString("mc-prefixes.rango." + rango.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%rangoPrefix%", "");

        TipoUsuario tipo = player.getTipoUsuario();
        if (tipo != null) formatedMessage = formatedMessage.replace("%tipoPrefix%", lang.getString("mc-prefixes.tipo." + tipo.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        formatedMessage = formatedMessage.replace("%player%", player.getNombrePublico());
        formatedMessage = formatedMessage.replace("%message%", message);

        return formatedMessage;
    }

    public static String getDsFormatedMessage(Player player, String message) {
        String formatedMessage = lang.getString("from-mc-message");

        formatedMessage = formatedMessage.replace("%mcPrefix%", lang.getString("ds-prefixes.mc"));

        Pais pais = player.getPaisPrefix();
        if (pais != null) formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("ds-prefixes.pais." + pais.getNombre()));
        else formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("ds-prefixes.pais.internacional"));

        RangoUsuario rango = player.getRangoUsuario();
        if (rango != null) formatedMessage = formatedMessage.replace("%rangoPrefix%", lang.getString("ds-prefixes.rango." + rango.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%rangoPrefix%", "");

        TipoUsuario tipo = player.getTipoUsuario();
        if (tipo != null) formatedMessage = formatedMessage.replace("%tipoPrefix%", lang.getString("ds-prefixes.tipo." + tipo.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        formatedMessage = formatedMessage.replace("%player%", player.getNombrePublico());
        formatedMessage = formatedMessage.replace("%message%", message);

        return formatedMessage;
    }

    public static String getDsFormatedMessage(Player player, String message, Pais dsPais) {
        String formatedMessage = lang.getString("ds-message");

        String dsPrefix = lang.getString("ds-prefixes.ds");
        dsPrefix = dsPrefix.replace("%paisLogo%", lang.getString("ds-prefixes.pais-logo." + dsPais.getNombre()));

        formatedMessage = formatedMessage.replace("%dsPais%", dsPrefix);

        Pais pais = player.getPaisPrefix();
        if (pais != null) formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("ds-prefixes.pais." + pais.getNombre()));
        else formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("ds-prefixes.pais.internacional"));

        RangoUsuario rango = player.getRangoUsuario();
        if (rango != null) formatedMessage = formatedMessage.replace("%rangoPrefix%", lang.getString("ds-prefixes.rango." + rango.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%rangoPrefix%", "");

        TipoUsuario tipo = player.getTipoUsuario();
        if (tipo != null) formatedMessage = formatedMessage.replace("%tipoPrefix%", lang.getString("ds-prefixes.tipo." + tipo.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        formatedMessage = formatedMessage.replace("%player%", player.getNombrePublico());
        formatedMessage = formatedMessage.replace("%message%", message);

        return formatedMessage;
    }

    public static String getDsFormatedMessage(String username, String message, Pais dsPais) {
        String formatedMessage = lang.getString("ds-message");

        String dsPrefix = lang.getString("ds-prefixes.ds");
        dsPrefix = dsPrefix.replace("%paisLogo%", lang.getString("ds-prefixes.pais-logo." + dsPais.getNombre()));

        formatedMessage = formatedMessage.replace("%dsPais%", dsPrefix);

        formatedMessage = formatedMessage.replace("%player%", username);
        formatedMessage = formatedMessage.replace("%message%", message);

        formatedMessage = formatedMessage.replace("%mcPrefix%", "");
        formatedMessage = formatedMessage.replace("%paisPrefix%", "");
        formatedMessage = formatedMessage.replace("%rangoPrefix%", "");
        formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        return formatedMessage;
    }

    public static MessageEmbed getServerStarted() {
        return new EmbedBuilder()
            .setTitle(lang.getString("ds-embeds.start.title"))
            .setDescription(lang.getString("ds-embeds.start.description"))
            .setColor(lang.getInt("ds-embeds.start.color"))
            .build();
    }

    public static MessageEmbed getServerStopped() {
        return new EmbedBuilder()
            .setTitle(lang.getString("ds-embeds.stop.title"))
            .setDescription(lang.getString("ds-embeds.stop.description"))
            .setColor(lang.getInt("ds-embeds.stop.color"))
            .build();
    }

    public static MessageEmbed getDsPlayerJoined(String playerName, UUID playerUUID) {
        String message = lang.getString("ds-embeds.player-join.message").replace("%player%", playerName);
        String avatarUrl = lang.getString("avatar-url").replace("%uuid%", playerUUID.toString());
        return new EmbedBuilder()
            .setAuthor(message, null, avatarUrl)
            .setColor(lang.getInt("ds-embeds.player-join.color"))
            .build();
    }

    public static MessageEmbed getDsPlayerLeft(String playerName, UUID playerUUID) {
        String message = lang.getString("ds-embeds.player-left.message").replace("%player%", playerName);
        String avatarUrl = lang.getString("avatar-url").replace("%uuid%", playerUUID.toString());
        return new EmbedBuilder()
            .setAuthor(message, null, avatarUrl)
            .setColor(lang.getInt("ds-embeds.player-left.color"))
            .build();
    }

    public static String getMcPlayerJoined(String playerName) {
        return lang.getString("player-join").replace("%player%", playerName);
    }

    public static String getMcPlayerLeft(String playerName) {
        return lang.getString("player-left").replace("%player%", playerName);
    }

    public static MessageEmbed getDsChatJoined(String playerName, UUID playerUUID) {
        String message = lang.getString("ds-embeds.chat-join.message").replace("%player%", playerName);
        String avatarUrl = lang.getString("avatar-url").replace("%uuid%", playerUUID.toString());
        return new EmbedBuilder()
            .setAuthor(message, null, avatarUrl)
            .setColor(lang.getInt("ds-embeds.chat-join.color"))
            .build();
    }

    public static MessageEmbed getDsChatLeft(String playerName, UUID playerUUID) {
        String message = lang.getString("ds-embeds.chat-left.message").replace("%player%", playerName);
        String avatarUrl = lang.getString("avatar-url").replace("%uuid%", playerUUID.toString());
        return new EmbedBuilder()
            .setAuthor(message, null, avatarUrl)
            .setColor(lang.getInt("ds-embeds.chat-left.color"))
            .build();
    }

    public static String getMcChatJoined(String playerName) {
        return lang.getString("chat-join").replace("%player%", playerName);
    }

    public static String getMcChatLeft(String playerName) {
        return lang.getString("chat-left").replace("%player%", playerName);
    }

}
