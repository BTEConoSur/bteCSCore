package com.bteconosur.core.util;

import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.LinkService;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Utilidad centralizada para envío de logs y notificaciones a Discord.
 * Permite registrar mensajes por país o globales, reportar a canales de staff
 * y notificar por mensaje privado a revisores, managers y desarrolladores.
 */
public class DiscordLogger {

    public static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    public static final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();

    private static boolean enableStaffConsoleLog = false;

    /**
     * Registra un mensaje en el canal de logs de un período.
     *
     * @param message mensaje a registrar.
     * @param pais país cuyo canal recibe el mensaje.
     */
    public static void countryLog(String message, Pais pais) {
        if (!config.getBoolean("discord-country-log")) return;
        MessageService.sendMessage(pais.getDsIdLog(), message);
        staffLog(message, pais);
    }

    /**
     * Registra un mensaje en múltiples canales de países.
     *
     * @param message mensaje a registrar.
     * @param paises países cuyos canales reciben el mensaje.
     */
    public static void countryLog(String message, List<Pais> paises) {
        if (!config.getBoolean("discord-country-log")) return;
        List<Long> channelIds = paises.stream().map(Pais::getDsIdLog).toList();
        MessageService.sendBroadcastMessage(channelIds, message);
    }

    /**
     * Registra un mensaje global en todos los canales de log de países.
     *
     * @param message mensaje a registrar.
     */
    public static void globalLog(String message) {
        if (!config.getBoolean("discord-country-log")) return;
        MessageService.sendBroadcastMessage(PaisRegistry.getInstance().getDsLogIds(), message);
    }

    /**
     * Registra un mensaje en el canal de staff.
     *
     * @param message mensaje a registrar.
     */
    public static void staffLog(String message) {
        if (!config.getBoolean("discord-staff-log")) return;
        MessageService.sendMessage(secret.getLong("discord-staff-log-id"), message);
    }

    /**
     * Registra un mensaje de país en staff aplicando una plantilla.
     *
     * @param message mensaje original.
     * @param pais país asociado al mensaje.
     */
    private static void staffLog(String message, Pais pais) {
        if (!config.getBoolean("discord-staff-log")) return;
        String formattedMessage = LanguageHandler.replaceDS("ds-staff-country-log", Language.getDefault(), pais).replace("%message%", message);
        MessageService.sendMessage(secret.getLong("discord-staff-log-id"), formattedMessage);
    }

    /**
     * Envía un embed al canal de consola de staff si está habilitado.
     *
     * @param embed embed a enviar.
     */
    public static void staffConsoleLog(MessageEmbed embed) {
        if (!enableStaffConsoleLog) return;
        if (!config.getBoolean("discord-staff-console-log")) return;
        MessageService.sendEmbed(secret.getLong("discord-staff-console-log-id"), embed);
    }

    /**
     * Notifica a managers de un país por Minecraft y, opcionalmente, por Discord privado.
     *
     * @param mcMessage mensaje mostrado en Minecraft.
     * @param dsMessage mensaje enviado por Discord privado.
     * @param pais país del que se notifican managers.
     * @param extraResolvers resolvedores extra para formatear el mensaje de Minecraft.
     */
    @SuppressWarnings("null")
    public static void notifyManagers(String mcMessage, String dsMessage, Pais pais, TagResolver... extraResolvers) {
        List<Player> managers = PlayerRegistry.getInstance().getManagers(pais);
        for (Player manager : managers) {
            if (LinkService.isPlayerLinked(manager) && manager.getConfiguration().getManagerDsNotifications()) {
                BTEConoSur.getDiscordManager().getJda().retrieveUserById(manager.getDsIdUsuario()).queue(user -> {
                    user.openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage(LanguageHandler.getText(manager.getLanguage(), "ds-manager-notification").replace("%mention%", user.getAsMention()).replace("%message%", dsMessage)).queue();
                    });
                });
            }
            PlayerLogger.info(manager, mcMessage, (String) null, extraResolvers);
        }
    }

    /**
     * Notifica a revisores de un país por Minecraft y, opcionalmente, por Discord privado.
     * Luego notifica también a managers del mismo país.
     *
     * @param mcMessage mensaje mostrado en Minecraft.
     * @param dsMessage mensaje enviado por Discord privado.
     * @param pais país del que se notifican revisores.
     * @param extraResolvers resolvedores extra para formatear el mensaje de Minecraft.
     */
    @SuppressWarnings("null")
    public static void notifyReviewers(String mcMessage, String dsMessage, Pais pais, TagResolver... extraResolvers) {
        List<Player> reviewers = PlayerRegistry.getInstance().getReviewers(pais);
        for (Player reviewer : reviewers) {
            if (LinkService.isPlayerLinked(reviewer) && reviewer.getConfiguration().getReviewerDsNotifications()) {
                BTEConoSur.getDiscordManager().getJda().retrieveUserById(reviewer.getDsIdUsuario()).queue(user -> {
                    user.openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage(LanguageHandler.getText(reviewer.getLanguage(), "ds-reviewer-notification").replace("%mention%", user.getAsMention()).replace("%message%", dsMessage)).queue();
                    });
                });
            }
            PlayerLogger.info(reviewer, mcMessage, (String) null, extraResolvers);
        }
        notifyManagers(mcMessage, dsMessage, pais, extraResolvers);
    }

    /**
     * Notifica a los desarrolladores mediante mención de rol en el canal de staff.
     *
     * @param message mensaje a incluir en la notificación.
     */
    public static void notifyDevs(String message) {
        if (!enableStaffConsoleLog) return;
        if (!config.getBoolean("discord-staff-console-log")) return;
        Guild guild = BTEConoSur.getDiscordManager().getJda().getGuildById(secret.getLong("discord-staff-guild-id")); 
        if (guild == null) {
            ConsoleLogger.warn("No se ha podido encontrar el Staff Hub en Discord.");
            return;
        }
        Role devRole = guild.getRoleById(secret.getLong("discord-dev-role-id"));
        if (devRole == null) {
            ConsoleLogger.warn("No se ha podido encontrar el rol de developers en el Staff Hub.");
            return;
        }
        String dsMessage = LanguageHandler.getText("ds-dev-notification").replace("%mention%", devRole.getAsMention()).replace("%message%", message);
        MessageService.sendMessage(secret.getLong("discord-staff-console-log-id"), dsMessage);
    }

    /**
     * Alterna el estado de envío de logs de consola al canal de staff.
     */
    public static void toggleStaffConsoleLog() {
        enableStaffConsoleLog = !enableStaffConsoleLog;
    }
}