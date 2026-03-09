package com.bteconosur.core.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;


import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;

/**
 * Utilidad para obtener cabezas personalizadas de la base de datos HeadDB.
 * Proporciona métodos para descargar cabezas por ID y cabezas de jugadores.
 */
public class HeadDBUtil implements Listener {

    private static boolean initialized = false;

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        ConsoleLogger.info("Inicializando HeadDatabase...");
        initialized = true;
    }

    /**
     * Obtiene una cabeza personalizada por su ID en HeadDatabase.
     *
     * @param headId identificador único de la cabeza en la base de datos.
     * @return ItemStack con la cabeza, o cabeza de jugador por defecto si no se encuentra.
     */
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

    /**
     * Obtiene la cabeza de un jugador usando su UUID.
     *
     * @param playerUUID UUID del jugador.
     * @param getOffline si {@code true} permite obtener cabezas de jugadores offline.
     * @return ItemStack con la cabeza del jugador, o cabeza por defecto si offline no está habilitado.
     */
    public static ItemStack getPlayerHead(UUID playerUUID, boolean getOffline) {
        if (!getOffline) {
            return ItemStack.of(Material.PLAYER_HEAD);
        }
        
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
        head.setItemMeta(meta);
        return head;
    }

}
