package com.bteconosur.core.util;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Utilidad para crear embeds de Discord para registros.
 * Construye embeds de error y advertencia con colores y títulos localizados.
 */
public class LoggerUtil {

    public static final YamlConfiguration embedColors = ConfigHandler.getInstance().getEmbedColors();

    /**
     * Crea un embed formateado de error con color configurado.
     *
     * @param description descripción del error a mostrar.
     * @return embed de error para enviar a Discord.
     */
    public static MessageEmbed getErrorEmbed(String description) {
        return new EmbedBuilder()
            .setTitle(LanguageHandler.getText("ds-embeds.error"))
            .setDescription(description)
            .setColor(embedColors.getInt("ds-embeds.error"))
            .build();
    }

    /**
     * Crea un embed formateado de advertencia con color configurado.
     *
     * @param description descripción de la advertencia a mostrar.
     * @return embed de advertencia para enviar a Discord.
     */
    public static MessageEmbed getWarnEmbed(String description) {
        return new EmbedBuilder()
            .setTitle(LanguageHandler.getText("ds-embeds.warn"))
            .setDescription(description)
            .setColor(embedColors.getInt("ds-embeds.warn"))
            .build();
    }

}
