package com.bteconosur.world.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.world.WorldExtent;
import com.bteconosur.world.WorldManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.eventbus.EventHandler;

public class WorldEditListener {

    private final WorldManager worldManager;
    private final DBManager dbManager;
    
    public WorldEditListener(WorldManager worldManager, DBManager dbManager) {
        this.worldManager = worldManager;
        this.dbManager = dbManager;
    }
    
    @Subscribe(priority = EventHandler.Priority.VERY_EARLY)
    public void onEditSession(@NotNull EditSessionEvent event) {
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            Player player = dbManager.get(Player.class, actor.getUniqueId()); // TODO: Ver si es mejor cachear los jugadores.
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
