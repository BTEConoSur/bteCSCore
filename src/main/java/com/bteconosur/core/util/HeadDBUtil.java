package com.bteconosur.core.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;

public class HeadDBUtil implements Listener {

    private static boolean initialized = false;

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        ConsoleLogger.info("Inicializando HeadDatabase...");
        initialized = true;
    }

    public static ItemStack get(String headId) {
        if (!initialized) {
            ConsoleLogger.warn("HeadDatabase no está inicializado. No se pueden obtener cabezas.");
            return ItemStack.of(Material.PLAYER_HEAD);
        }
        HeadDatabaseAPI api = new HeadDatabaseAPI();

        try {
            ItemStack head = api.getItemHead(headId);
            if (head == null) {
                ConsoleLogger.warn("No se ha encontrado la cabeza con ID: " + headId);
                return ItemStack.of(Material.PLAYER_HEAD);
            }
            return head;
        } catch (NullPointerException nullPointerException) {
            ConsoleLogger.warn("No se ha encontrado la cabeza con ID: " + headId);
        }

        return ItemStack.of(Material.PLAYER_HEAD);
    }

    public static ItemStack getPlayerHead(UUID playerUUID) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(playerUUID);
        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }

}
