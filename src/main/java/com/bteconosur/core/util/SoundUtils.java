package com.bteconosur.core.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.bteconosur.core.config.ConfigHandler;

public class SoundUtils {

    private static final YamlConfiguration sound = ConfigHandler.getInstance().getSound();

    private static Sound getSound(String key) {
        String soundName = sound.getString("sound-effects." + key + ".particle", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            return  Registry.SOUNDS.get(NamespacedKey.minecraft(soundName));
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }

    private static float getVolume(String key) {
        return (float) sound.getDouble("sound-effects." + key + ".volume", 1.0);
    }

    private static float getPitch(String key) {
        return (float) sound.getDouble("sound-effects." + key + ".pitch", 1.0);
    }

    public static void playSound(Location location, String key) {
        location.getWorld().playSound(location, getSound(key), getVolume(key), getPitch(key));
    }

    public static void playSound(Player player, String key) {
        player.playSound(player.getLocation(), getSound(key), getVolume(key), getPitch(key));
    }

}
