package com.bteconosur.world.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

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

}
