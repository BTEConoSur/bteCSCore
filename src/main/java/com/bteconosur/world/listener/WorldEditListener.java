package com.bteconosur.world.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldExtent;
import com.bteconosur.world.WorldManager;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.eventbus.EventHandler;

public class WorldEditListener {

    private final WorldManager worldManager;
    private final PlayerRegistry playerRegistry;
    
    public WorldEditListener(WorldManager worldManager) {
        this.worldManager = worldManager;
        this.playerRegistry = PlayerRegistry.getInstance();
    }
    
    @Subscribe(priority = EventHandler.Priority.VERY_EARLY)
    public void onEditSession(@NotNull EditSessionEvent event) {
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            Player player = playerRegistry.get(actor.getUniqueId());
            if (player == null) return;
            com.sk89q.worldedit.world.World weWorld = event.getWorld();
            if (weWorld == null) return;

            String worldName = weWorld.getName();
            World bukkitWorld = Bukkit.getWorld(worldName);
            if (bukkitWorld == null) return;
        
            event.setExtent(new WorldExtent(event.getExtent(), worldManager, player, bukkitWorld));
        }
    }
}
