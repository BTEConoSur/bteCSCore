package com.bteconosur.core.util;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

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

}
