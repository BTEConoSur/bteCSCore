package com.bteconosur.world.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldManager;

public class MovingListeners implements Listener {

    private final WorldManager worldManager;

    public MovingListeners() {
        this.worldManager = WorldManager.getInstance();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        worldManager.checkMove(event.getTo(), event.getPlayer());
        worldManager.checkTitles(event.getTo(), event.getPlayer());
        if (!worldManager.checkPaisMove(event.getFrom(), event.getTo(), event.getPlayer())) {
            event.setCancelled(true);
            Player player = Player.getBTECSPlayer(event.getPlayer());
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "cant-leave-paises"), (String) null);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PlayerRegistry.updateLastLocation(event.getPlayer().getUniqueId(), event.getFrom());
        if (event.getFrom().getWorld() != event.getTo().getWorld()) return;
        worldManager.checkMove(event.getTo(), event.getPlayer());
        if (!worldManager.checkPaisMove(event.getFrom(), event.getTo(), event.getPlayer())) {
            event.setCancelled(true);
            Player player = Player.getBTECSPlayer(event.getPlayer());
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "cant-leave-paises"), (String) null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerRegistry.removeLastLocation(event.getPlayer().getUniqueId());
        worldManager.getBTEWorld().clearPlayerTasks(event.getPlayer().getUniqueId()); 
    }

}
