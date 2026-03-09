package com.bteconosur.discord.util;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Clase de utilidad para validar elementos de Discord.
 * Proporciona métodos estáticos para verificar la validez de canales, usuarios, mensajes y embeds.
 */
public final class DiscordValidate {

    /**
     * Valida que JDA esté inicializado y disponible.
     * 
     * @return true si JDA está disponible, false en caso contrario
     */
    public static boolean jda() {
        if (BTEConoSur.getDiscordManager() == null || BTEConoSur.getDiscordManager().getJda() == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.not-jda"));
            return false;
        }
        return true;
    }   

    /**
     * Valida que el identificador de canal sea válido.
     * 
     * @param channelId Identificador del canal a validar
     * @return true si el ID es válido, false en caso contrario
     */
    public static boolean channelId(Long channelId) {
        if (channelId == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-channel-id"));
            return false;
        }
        return true;
    }

    /**
     * Valida que el identificador de usuario sea válido.
     * 
     * @param userId Identificador del usuario a validar
     * @return true si el ID es válido, false en caso contrario
     */
    public static boolean userId(Long userId) {
        if (userId == null) {
            //ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-user-id"));
            return false;
        }
        return true;
    }

    /**
     * Valida que el objeto usuario sea válido.
     * 
     * @param user Usuario a validar
     * @return true si el usuario es válido, false en caso contrario
     */
    public static boolean user(User user) {
        if (user == null) {
            //ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-user"));
            return false;
        }
        return true;
    }

    /**
     * Valida que el contenido del mensaje no esté vacío.
     * 
     * @param message Contenido del mensaje a validar
     * @return true si el mensaje es válido, false en caso contrario
     */
    public static boolean messageContent(String message) {
        if (message == null || message.isEmpty()) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-message"));
            return false;
        }
        return true;
    }

    /**
     * Valida que el canal de texto sea válido.
     * 
     * @param channel Canal de texto a validar
     * @return true si el canal es válido, false en caso contrario
     */
    public static boolean channel(TextChannel channel) {
        if (channel == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-channel"));
            return false;
        }
        return true;
    }

    /**
     * Valida que el identificador de mensaje sea válido.
     * 
     * @param messageId Identificador del mensaje a validar
     * @return true si el ID es válido, false en caso contrario
     */
    public static boolean messageId(Long messageId) {
        if (messageId == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-message-id"));
            return false;
        }
        return true;
    }

    /**
     * Valida que el embed de mensaje sea válido.
     * 
     * @param embed Embed a validar
     * @return true si el embed es válido, false en caso contrario
     */
    public static boolean embed(MessageEmbed embed) {
        if (embed == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.invalid-embed"));
            return false;
        }
        return true;
    }
}
