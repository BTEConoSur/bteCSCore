package com.bteconosur.discord.util;

import java.util.HashMap;
import java.util.Map;

import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.IDUtils;
import com.bteconosur.discord.DiscordManager;

import net.dv8tion.jda.api.entities.User;

/**
 * Servicio para gestionar la vinculación entre cuentas de Minecraft y Discord.
 * Maneja códigos de vinculación temporales y la asociación de cuentas.
 */
public class LinkService {

    private static Map<Player, String> minecraftCodes = new HashMap<>();
    private static Map<Long, String> discordCodes = new HashMap<>() ;

    /**
     * Valida que un ID de usuario de Discord sea válido y exista.
     * 
     * @param discordId ID del usuario de Discord
     * @return true si el usuario existe, false en caso contrario
     */
    public static boolean isValidUserId(Long discordId) {
        if (discordId == null || discordId <= 0) return false;
        try {
            User user = DiscordManager.getInstance().getJda().retrieveUserById(discordId).complete();
            return user != null;
        } catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * Verifica si un código de Minecraft es válido.
     * 
     * @param code Código a verificar
     * @return true si el código existe, false en caso contrario
     */
    public static boolean isMinecraftCodeValid(String code) {
        return minecraftCodes.containsValue(code);
    }

    /**
     * Verifica si un código de Discord es válido.
     * 
     * @param code Código a verificar
     * @return true si el código existe, false en caso contrario
     */
    public static boolean isDiscordCodeValid(String code) {
        return discordCodes.containsValue(code);
    }

    /**
     * Verifica si un jugador tiene un código de Minecraft pendiente.
     * 
     * @param player Jugador a verificar
     * @return true si tiene un código pendiente, false en caso contrario
     */
    public static boolean hasMinecraftCode(Player player) {
        return minecraftCodes.containsKey(player);
    }

    /**
     * Verifica si un ID de Discord tiene un código pendiente.
     * 
     * @param discordId ID de Discord a verificar
     * @return true si tiene un código pendiente, false en caso contrario
     */
    public static boolean hasDiscordCode(Long discordId) {
        return discordCodes.containsKey(discordId);
    }

    /**
     * Obtiene el código de Minecraft de un jugador.
     * 
     * @param player Jugador del cual obtener el código
     * @return El código o null si no existe
     */
    public static String getMinecraftCode(Player player) {
        return minecraftCodes.get(player);
    }

    /**
     * Obtiene el código de Discord de un usuario.
     * 
     * @param discordId ID del usuario de Discord
     * @return El código o null si no existe
     */
    public static String getDiscordCode(Long discordId) {
        return discordCodes.get(discordId);
    }

    /**
     * Verifica si un jugador ya está vinculado a Discord.
     * 
     * @param player Jugador a verificar
     * @return true si está vinculado, false en caso contrario
     */
    public static boolean isPlayerLinked(Player player) {
        return player.getDsIdUsuario() != null;
    }

    /**
     * Genera un código de vinculación para un jugador de Minecraft.
     * 
     * @param player Jugador para el cual generar el código
     * @return El código generado
     */
    public static String generateMinecraftCode(Player player) {
        String code = IDUtils.generarCodigoLink();
        minecraftCodes.put(player, code);
        return code;
    }

    /**
     * Genera un código de vinculación para un usuario de Discord.
     * 
     * @param discordId ID del usuario de Discord
     * @return El código generado
     */
    public static String generateDiscordCode(Long discordId) {
        String code = IDUtils.generarCodigoLink();
        discordCodes.put(discordId, code);
        return code;
    }

    /**
     * Vincula una cuenta de Discord usando un código de Minecraft.
     * 
     * @param minecraftCode Código de vinculación de Minecraft
     * @param discordId ID del usuario de Discord
     * @return El jugador vinculado actualizado, o null si falla
     */
    public static Player linkDiscord(String minecraftCode, Long discordId) {
        if (!isMinecraftCodeValid(minecraftCode)) return null;
        Player player = minecraftCodes.entrySet().stream()
            .filter(entry -> minecraftCode.equals(entry.getValue()))
            .map(Map.Entry::getKey).findFirst().orElse(null);
        if (player.getDsIdUsuario() != null) return null;

        player.setDsIdUsuario(discordId);
        minecraftCodes.remove(player);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }


    /**
     * Vincula directamente una cuenta de Discord a un jugador.
     * 
     * @param discordId ID del usuario de Discord
     * @param player Jugador a vincular
     * @return El jugador actualizado
     */
    public static Player link(Long discordId, Player player) {
        player.setDsIdUsuario(discordId);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    /**
     * Desvincula la cuenta de Discord de un jugador.
     * 
     * @param player Jugador a desvincular
     * @return El jugador actualizado
     */
    public static Player unlink(Player player) {
        player.setDsIdUsuario(null);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    /**
     * Vincula una cuenta de Minecraft usando un código de Discord.
     * 
     * @param discordCode Código de vinculación de Discord
     * @param player Jugador a vincular
     */
    public static void linkMinecraft(String discordCode, Player player) {
        if (!isDiscordCodeValid(discordCode)) return;
        Long discordId = discordCodes.entrySet().stream()
            .filter(entry -> discordCode.equals(entry.getValue()))
            .map(Map.Entry::getKey).findFirst().orElse(null);
        if (player.getDsIdUsuario() != null) return;

        player.setDsIdUsuario(discordId);
        discordCodes.remove(discordId);
        PlayerRegistry.getInstance().merge(player.getUuid());
    }

}
