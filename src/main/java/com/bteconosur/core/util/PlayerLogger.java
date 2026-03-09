package com.bteconosur.core.util;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.json.JsonUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Utilidad centralizada para registrar mensajes dirigidos a jugadores.
 * Proporciona métodos para enviar mensajes a Minecraft y notificaciones
 * a Discord sincrónicamente cuando está habilitada la opción.
 */
public class PlayerLogger {

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    /**
     * Registra un mensaje de información a un jugador (emisor de comando).
     *
     * @param sender jugador que ejecutó el comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void info(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        info(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Registra un mensaje de información para un emisor y una notificación embebida opcional en Discord.
     * Si el emisor no es un jugador, el mensaje se registra en consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void info(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        info(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Registra un mensaje de información para un jugador y una notificación privada opcional en Discord.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void info(Player player, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-info").replace("%pluginPrefix%", LanguageHandler.getText(player.getLanguage(), "plugin-prefix")).replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Registra un mensaje de información para un jugador y una notificación embebida opcional en Discord.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void info(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-info").replace("%pluginPrefix%", LanguageHandler.getText(player.getLanguage(), "plugin-prefix")).replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Registra un mensaje de información con serialización de objeto para un emisor.
     * Si el emisor no es un jugador, el registro se envía a consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void info(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage, object); return; }
        info(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object, resolvers);
    }

    /**
     * Registra un mensaje de información con un objeto serializado en JSON para un jugador.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void info(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (player == null) return;
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-info").replace("%pluginPrefix%", LanguageHandler.getText(player.getLanguage(), "plugin-prefix")).replace("%message%", mcMessage) + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    /**
     * Registra un mensaje de debug para un emisor y una notificación privada opcional en Discord.
     * Solo se ejecuta cuando el modo debug está habilitado.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void debug(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!config.getBoolean("debug-mode", false)) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage); return; }
        debug(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Registra un mensaje de debug para un emisor y un embed opcional en Discord.
     * Solo se ejecuta cuando el modo debug está habilitado.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void debug(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!config.getBoolean("debug-mode", false)) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage); return; }
        debug(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Registra un mensaje de debug para un jugador y una notificación privada opcional en Discord.
     * Solo se ejecuta cuando el modo debug está habilitado.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void debug(Player player, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (!config.getBoolean("debug-mode", false)) return;
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-debug").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Registra un mensaje de debug para un jugador y un embed opcional en Discord.
     * Solo se ejecuta cuando el modo debug está habilitado.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void debug(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (!config.getBoolean("debug-mode", false)) return;
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-debug").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Registra un mensaje de debug con serialización de objeto para un emisor.
     * Solo se ejecuta cuando el modo debug está habilitado.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void debug(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (sender == null) return;
        if (!config.getBoolean("debug-mode", false)) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.debug(mcMessage, object); return; }
        debug(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object);
    }

    /**
     * Registra un mensaje de debug con un objeto serializado en JSON para un jugador.
     * Solo se ejecuta cuando el modo debug está habilitado.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void debug(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (player == null) return;
        if (!config.getBoolean("debug-mode", false)) return;
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-debug").replace("%message%", mcMessage) + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    /**
     * Registra un mensaje de advertencia para un emisor y una notificación privada opcional en Discord.
     * Si el emisor no es un jugador, el mensaje se registra en consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void warn(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        warn(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Registra un mensaje de advertencia para un emisor y un embed opcional en Discord.
     * Si el emisor no es un jugador, el mensaje se registra en consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void warn(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        warn(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Registra un mensaje de advertencia para un jugador y una notificación privada opcional en Discord.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void warn(Player player, String mcMessage, String dsMessage, TagResolver... resolvers ) {
        if (player == null) return;
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-warn").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Registra un mensaje de advertencia para un jugador y un embed opcional en Discord.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void warn(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-warn").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Registra un mensaje de advertencia con serialización de objeto para un emisor.
     * Si el emisor no es un jugador, el registro se envía a consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void warn(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage, object); return; }
        warn(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object, resolvers);
    }

    /**
     * Registra un mensaje de advertencia con un objeto serializado en JSON para un jugador.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void warn(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (player == null) return;
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-warn").replace("%message%", mcMessage) + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    /**
     * Registra un mensaje de error para un emisor y una notificación privada opcional en Discord.
     * Si el emisor no es un jugador, el mensaje se registra en consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void error(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        error(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Registra un mensaje de error para un emisor y un embed opcional en Discord.
     * Si el emisor no es un jugador, el mensaje se registra en consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void error(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage); return; }
        error(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Registra un mensaje de error para un jugador y una notificación privada opcional en Discord.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void error(Player player, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-error").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Registra un mensaje de error para un jugador y un embed opcional en Discord.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void error(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-error").replace("%message%", mcMessage), player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Registra un mensaje de error con serialización de objeto para un emisor.
     * Si el emisor no es un jugador, el registro se envía a consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void error(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.info(mcMessage, object); return; }
        error(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object, resolvers);
    }

    /**
     * Registra un mensaje de error con un objeto serializado en JSON para un jugador.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void error(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (player == null) return;
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(LanguageHandler.getText(player.getLanguage(), "player-error").replace("%message%", mcMessage) + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    /**
     * Envía un mensaje genérico a un emisor y una notificación privada opcional en Discord.
     * Si el emisor no es un jugador, el mensaje se envía a consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void send(CommandSender sender, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.send(mcMessage); return; }
        send(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Envía un mensaje genérico a un emisor y un embed opcional por Discord.
     * Si el emisor no es un jugador, el mensaje se envía a consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void send(CommandSender sender, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.send(mcMessage); return; }
        send(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, resolvers);
    }

    /**
     * Envía un mensaje genérico a un jugador y una notificación privada opcional en Discord.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void send(Player player, String mcMessage, String dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (mcMessage != null) sendMc(mcMessage, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Envía un mensaje genérico a un jugador y un embed opcional por Discord.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage embed de Discord a enviar por privado, o {@code null}.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void send(Player player, String mcMessage, MessageEmbed dsMessage, TagResolver... resolvers) {
        if (player == null) return;
        if (mcMessage != null) sendMc(mcMessage, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendEmbedDM(player.getDsIdUsuario(), dsMessage);
    }

    /**
     * Envía un mensaje genérico con serialización de objeto para un emisor.
     * Si el emisor no es un jugador, el registro se envía a consola.
     *
     * @param sender emisor del comando.
     * @param mcMessage mensaje de Minecraft a mostrar.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void send(CommandSender sender, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (sender == null) return;
        if (!(sender instanceof org.bukkit.entity.Player bukkitPlayer)) { ConsoleLogger.send(mcMessage, object); return; }
        send(PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId()), mcMessage, dsMessage, object, resolvers);
    }   

    /**
     * Envía un mensaje genérico con un objeto serializado en JSON para un jugador.
     *
     * @param player jugador que recibirá el mensaje.
     * @param mcMessage mensaje de Minecraft a mostrar, o {@code null}.
     * @param dsMessage mensaje de Discord a enviar por privado, o {@code null}.
     * @param object objeto adicional que se serializa a JSON.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void send(Player player, String mcMessage, String dsMessage, Object object, TagResolver... resolvers) {
        if (player == null) return;
        String json = JsonUtils.toJson(object);
        if (mcMessage != null) sendMc(mcMessage + "\n" + json, player, resolvers);
        if (dsMessage != null && checkSimultaneousNotifications(player)) MessageService.sendDM(player.getDsIdUsuario(), dsMessage + "\n" + json);
    }

    /**
     * Envía un mensaje MiniMessage a un jugador del registro, si se encuentra en línea.
     *
     * @param message mensaje a deserializar y enviar.
     * @param player jugador destinatario del registro interno.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    private static void sendMc(String message, Player player, TagResolver... resolvers) {
        if (PlayerRegistry.getInstance().isOnline(player.getUuid())) {
            player.getBukkitPlayer().sendMessage(MiniMessage.miniMessage().deserialize(message, resolvers));
        }
    }

    /**
     * Envía un mensaje MiniMessage a un jugador de Bukkit si se encuentra en línea.
     *
     * @param message mensaje a deserializar y enviar.
     * @param player jugador de Bukkit destinatario.
     * @param resolvers resolvedores de tags para personalizar el mensaje.
     */
    public static void sendMc(String message, org.bukkit.entity.Player player, TagResolver... resolvers) {
        if (PlayerRegistry.getInstance().isOnline(player.getUniqueId())) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(message, resolvers));
        }
    }

    /**
     * Verifica si deben enviarse notificaciones simultáneas por Discord.
     *
     * @param player jugador a evaluar.
     * @return {@code true} si el jugador está desconectado o tiene habilitadas las notificaciones simultáneas.
     */
    private static boolean checkSimultaneousNotifications(Player player) {
        return !PlayerRegistry.getInstance().isOnline(player.getUuid()) || player.getConfiguration().getGeneralSimultaneousNotifications();
    }

}
