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
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class DiscordLogger {

    public static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    public static final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();

    private static boolean enableStaffConsoleLog = false;

    public static void countryLog(String message, Pais pais) {
        if (!config.getBoolean("discord-country-log")) return;
        MessageService.sendMessage(pais.getDsIdLog(), message);
        staffLog(message, pais);
    }

    public static void countryLog(String message, List<Pais> paises) {
        if (!config.getBoolean("discord-country-log")) return;
        List<Long> channelIds = paises.stream().map(Pais::getDsIdLog).toList();
        MessageService.sendBroadcastMessage(channelIds, message);
    }

    public static void globalLog(String message) {
        if (!config.getBoolean("discord-country-log")) return;
        MessageService.sendBroadcastMessage(PaisRegistry.getInstance().getDsLogIds(), message);
    }

    public static void staffLog(String message) {
        if (!config.getBoolean("discord-staff-log")) return;
        MessageService.sendMessage(secret.getLong("discord-staff-log-id"), message);
    }

    private static void staffLog(String message, Pais pais) {
        if (!config.getBoolean("discord-staff-log")) return;
        String formattedMessage = LanguageHandler.replaceDS("ds-staff-country-log", Language.getDefault(), pais).replace("%message%", message);
        MessageService.sendMessage(secret.getLong("discord-staff-log-id"), formattedMessage);
    }

    public static void staffConsoleLog(MessageEmbed embed) {
        if (!enableStaffConsoleLog) return;
        if (!config.getBoolean("discord-staff-console-log")) return;
        MessageService.sendEmbed(secret.getLong("discord-staff-console-log-id"), embed);
    }

    public static void notifyManagers(String mcMessage, String dsMessage, Pais pais, TagResolver... extraResolvers) {
        List<Player> managers = PlayerRegistry.getInstance().getManagers(pais);
        for (Player manager : managers) {
            User user = Player.getDsUser(manager);
            if (user == null) {
                ConsoleLogger.warn("El Manager '" + manager.getNombre() + "' no tiene la cuenta de Discord enlazada.");
                continue;
            }
            String dsFormat = null;
            if (manager.getConfiguration().getManagerDsNotifications()) dsFormat = LanguageHandler.getText(manager.getLanguage(), "ds-manager-notification").replace("%mention%", user.getAsMention()).replace("%message%", dsMessage);
            PlayerLogger.info(manager, mcMessage, dsFormat, extraResolvers);
        }
    }

    public static void notifyReviewers(String mcMessage, String dsMessage, Pais pais, TagResolver... extraResolvers) {
        List<Player> reviewers = PlayerRegistry.getInstance().getReviewers(pais);
        for (Player reviewer : reviewers) {
            
            User user = Player.getDsUser(reviewer);
            if (user == null) {
                ConsoleLogger.warn("El Reviewer '" + reviewer.getNombre() + "' no tiene la cuenta de Discord enlazada.");
                continue;
            };
            String dsFormat = null;
            if (reviewer.getConfiguration().getReviewerDsNotifications()) dsFormat = LanguageHandler.getText(reviewer.getLanguage(), "ds-reviewer-notification").replace("%mention%", user.getAsMention()).replace("%message%", dsMessage);
            PlayerLogger.info(reviewer, mcMessage, dsFormat, extraResolvers);
        }
        notifyManagers(mcMessage, dsMessage, pais, extraResolvers);
    }

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

    public static void toggleStaffConsoleLog() {
        enableStaffConsoleLog = !enableStaffConsoleLog;
    }
}