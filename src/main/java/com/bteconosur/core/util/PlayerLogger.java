package com.bteconosur.core.util;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class PlayerLogger {

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void info(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        info(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void info(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        info(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void info(Player player, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (mcMessage != null) sendMc(lang.getString("player-info").replace("%pluginPrefix%", lang.getString("plugin-prefix")).replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void info(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (mcMessage != null) sendMc(lang.getString("player-info").replace("%pluginPrefix%", lang.getString("plugin-prefix")).replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void info(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage, object); return; }
        info(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object, resolvers);
    }

    public static void info(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(lang.getString("player-info").replace("%pluginPrefix%", lang.getString("plugin-prefix")).replace("%message%", mcMessage) + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    public static void debug(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (!config.getBoolean("debug-mode", false)) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage); return; }
        debug(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void debug(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (!config.getBoolean("debug-mode", false)) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage); return; }
        debug(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void debug(Player player, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (!config.getBoolean("debug-mode", false)) return;
        if (mcMessage != null) sendMc(lang.getString("player-debug").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void debug(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (!config.getBoolean("debug-mode", false)) return;
        if (mcMessage != null) sendMc(lang.getString("player-debug").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void debug(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (!config.getBoolean("debug-mode", false)) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage, object); return; }
        debug(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object);
    }

    public static void debug(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (!config.getBoolean("debug-mode", false)) return;
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(lang.getString("player-debug").replace("%message%", mcMessage) + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    public static void warn(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        warn(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void warn(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        warn(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void warn(Player player, String mcMessage, String dsMessage, TagResolver... resolvers ) {
        if (mcMessage != null) sendMc(lang.getString("player-warn").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void warn(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (mcMessage != null) sendMc(lang.getString("player-warn").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void warn(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage, object); return; }
        warn(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object, resolvers);
    }

    public static void warn(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(lang.getString("player-warn").replace("%message%", mcMessage) + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    public static void error(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        error(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void error(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        error(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void error(Player player, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (mcMessage != null) sendMc(lang.getString("player-error").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void error(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (mcMessage != null) sendMc(lang.getString("player-error").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void error(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage, object); return; }
        error(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object, resolvers);
    }

    public static void error(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(lang.getString("player-error").replace("%message%", mcMessage) + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    public static void send(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage); return; }
        send(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void send(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage); return; }
        send(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    public static void send(Player player, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (mcMessage != null) sendMc(mcMessage, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void send(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (mcMessage != null) sendMc(mcMessage, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    public static void send(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage, object); return; }
        send(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object, resolvers);
    }   

    public static void send(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(mcMessage + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    private static void sendMc(String message, Player player, TagResolver... resolvers) {
        if (PlayerRegistry.getInstance().isOnline(player.getUuid())) {
            player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(message, resolvers));
        }
    }

    private static boolean checkSimultaneousNotifications(Player player) {
        return !PlayerRegistry.getInstance().isOnline(player.getUuid()) || player.getConfiguration().getGeneralSimultaneousNotifications();
    }

}
