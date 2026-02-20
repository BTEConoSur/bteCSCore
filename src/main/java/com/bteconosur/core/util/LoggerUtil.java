package com.bteconosur.core.util;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class LoggerUtil {

    public static final YamlConfiguration embedColors = ConfigHandler.getInstance().getEmbedColors();

    public static MessageEmbed getErrorEmbed(String description) {
        return new EmbedBuilder()
            .setTitle(LanguageHandler.getText("ds-embeds.error"))
            .setDescription(description)
            .setColor(embedColors.getInt("ds-embeds.error"))
            .build();
    }

    public static MessageEmbed getWarnEmbed(String description) {
        return new EmbedBuilder()
            .setTitle(LanguageHandler.getText("ds-embeds.warn"))
            .setDescription(description)
            .setColor(embedColors.getInt("ds-embeds.warn"))
            .build();
    }

}
