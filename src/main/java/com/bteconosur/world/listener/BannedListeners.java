package com.bteconosur.world.listener;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;


public class BannedListeners implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ArmorStand) return;
        if (event.getEntity() instanceof TextDisplay) return;
        if (event.getEntity() instanceof ItemDisplay) return;
        if (event.getEntity() instanceof BlockDisplay) return;
        event.setCancelled(true);
    }

}