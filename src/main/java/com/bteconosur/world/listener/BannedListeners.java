package com.bteconosur.world.listener;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * Listener que controla el spawn de entidades, permitiendo solo tipos específicos.
 * Previene el spawn de entidades no permitidas como mobs, vehículos, etc.
 */
public class BannedListeners implements Listener {

    /**
     * Cancela el spawn de entidades excepto las permitidas.
     * Entidades permitidas: ArmorStand, TextDisplay, ItemDisplay, BlockDisplay, ItemFrame, Painting.
     * 
     * @param event Evento de spawn de entidad
     */
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ArmorStand) return;
        if (event.getEntity() instanceof TextDisplay) return;
        if (event.getEntity() instanceof ItemDisplay) return;
        if (event.getEntity() instanceof BlockDisplay) return;
        if (event.getEntity() instanceof ItemFrame) return;
        if (event.getEntity() instanceof Painting) return;
        event.setCancelled(true);
    }

}