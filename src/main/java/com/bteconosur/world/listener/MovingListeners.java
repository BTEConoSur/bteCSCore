package com.bteconosur.world.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.bteconosur.world.WorldManager;

public class MovingListeners implements Listener {

    private final WorldManager worldManager;

    public MovingListeners(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        worldManager.checkMove(event.getFrom(), event.getTo(), event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() != event.getTo().getWorld()) return;
        worldManager.checkMove(event.getFrom(), event.getTo(), event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        worldManager.getBTEWorld().clearPlayerTasks(event.getPlayer().getUniqueId()); 
    }

}
