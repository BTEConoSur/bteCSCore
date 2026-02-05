package com.bteconosur.discord.util;

import java.util.HashMap;
import java.util.Map;

import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.IDUtils;
import com.bteconosur.discord.DiscordManager;

import net.dv8tion.jda.api.entities.User;

public class LinkService {

    private static Map<Player, String> minecraftCodes = new HashMap<>();
    private static Map<Long, String> discordCodes = new HashMap<>() ;

    public static boolean isValidUserId(Long discordId) {
        if (discordId == null || discordId <= 0) return false;
        try {
            User user = DiscordManager.getInstance().getJda().retrieveUserById(discordId).complete();
            return user != null;
        } catch (Exception ex) {
            return false;
        }
    }
    
    public static boolean isMinecraftCodeValid(String code) {
        return minecraftCodes.containsValue(code);
    }

    public static boolean isDiscordCodeValid(String code) {
        return discordCodes.containsValue(code);
    }

    public static boolean hasMinecraftCode(Player player) {
        return minecraftCodes.containsKey(player);
    }

    public static boolean hasDiscordCode(Long discordId) {
        return discordCodes.containsKey(discordId);
    }

    public static String getMinecraftCode(Player player) {
        return minecraftCodes.get(player);
    }

    public static String getDiscordCode(Long discordId) {
        return discordCodes.get(discordId);
    }

    public static boolean isPlayerLinked(Player player) {
        return player.getDsIdUsuario() != null;
    }

    public static String generateMinecraftCode(Player player) {
        String code = IDUtils.generarCodigoLink();
        minecraftCodes.put(player, code);
        return code;
    }

    public static String generateDiscordCode(Long discordId) {
        String code = IDUtils.generarCodigoLink();
        discordCodes.put(discordId, code);
        return code;
    }

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


    public static Player link(Long discordId, Player player) {
        player.setDsIdUsuario(discordId);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    public static Player unlink(Player player) {
        player.setDsIdUsuario(null);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

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
