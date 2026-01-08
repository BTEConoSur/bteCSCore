package com.bteconosur.core.util;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class LoggerUtil {

    public static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public static MessageEmbed getErrorEmbed(String description) {
        return new EmbedBuilder()
            .setTitle(lang.getString("ds-embeds.error.title"))
            .setDescription(description)
            .setColor(lang.getInt("ds-embeds.error.color"))
            .build();
    }

    public static MessageEmbed getWarnEmbed(String description) {
        return new EmbedBuilder()
            .setTitle(lang.getString("ds-embeds.warn.title"))
            .setDescription(description)
            .setColor(lang.getInt("ds-embeds.warn.color"))
            .build();
    }

}
