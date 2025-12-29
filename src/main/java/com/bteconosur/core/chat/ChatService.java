package com.bteconosur.core.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.discord.util.MessageService;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class ChatService {
    
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

        formatedMessage = formatedMessage.replace("%dsPrefix%", "");

        return formatedMessage;
    }

    public static String getMcFormatedMessage(String username, String message, Pais dsPais) {
        String formatedMessage = lang.getString("mc-message");

        String dsPrefix = lang.getString("mc-prefixes.ds");
        dsPrefix = dsPrefix.replace("%pais%", dsPais.getNombrePublico());

        formatedMessage = formatedMessage.replace("%dsPrefix%", dsPrefix);
        formatedMessage = formatedMessage.replace("%player%", username);
        formatedMessage = formatedMessage.replace("%message%", message);

        formatedMessage = formatedMessage.replace("%paisPrefix%", "");
        formatedMessage = formatedMessage.replace("%rangoPrefix%", "");
        formatedMessage = formatedMessage.replace("%tipoPrefix% ", "");

        return formatedMessage;
    }

    public static String getMcFormatedMessage(Player player, String message, Pais dsPais) {
        String formatedMessage = lang.getString("mc-message");

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

        formatedMessage = formatedMessage.replace("%paisPrefix%", "");

        return formatedMessage;
    }

    public static String getDsFormatedMessage(Player player, String message) {
        String formatedMessage = lang.getString("ds-message");

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

        formatedMessage = formatedMessage.replace("%dsPais%", "");

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

        formatedMessage = formatedMessage.replace("%mcPrefix%", "");
        formatedMessage = formatedMessage.replace("%paisPrefix%", "");

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
        formatedMessage = formatedMessage.replace("%tipoPrefix% ", "");

        return formatedMessage;
    }

    public static void broadcastMessage(String dsMessage, String mcMessage, Long dsFrom) {
        List<Long> ids = new ArrayList<>(PaisRegistry.getInstance().getDsGlobalChatIds());
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
        }
        ids.remove(dsFrom);
        MessageService.sendBroadcastMessage(ids, dsMessage);
    }

    public static void broadcastMessage(String dsMessage, String mcMessage) {
        List<Long> ids = PaisRegistry.getInstance().getDsGlobalChatIds();
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(mcMessage));
        }
        MessageService.sendBroadcastMessage(ids, dsMessage);
    }
}
