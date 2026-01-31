package com.bteconosur.world.listener;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.world.WorldManager;

public class MovingListeners implements Listener {

    private final WorldManager worldManager;
    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public MovingListeners() {
        this.worldManager = WorldManager.getInstance();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        worldManager.checkLayerMove(event.getFrom(), event.getTo(), event.getPlayer());
        if (!worldManager.checkPaisMove(event.getFrom(), event.getTo(), event.getPlayer())) {
            event.setCancelled(true);
            Player player = Player.getBTECSPlayer(event.getPlayer());
            PlayerLogger.warn(player, lang.getString("cant-leave-paises"), (String) null);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() != event.getTo().getWorld()) return;
        worldManager.checkLayerMove(event.getFrom(), event.getTo(), event.getPlayer());
        if (!worldManager.checkPaisMove(event.getFrom(), event.getTo(), event.getPlayer())) {
            event.setCancelled(true);
            Player player = Player.getBTECSPlayer(event.getPlayer());
            PlayerLogger.warn(player, lang.getString("cant-leave-paises"), (String) null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        worldManager.getBTEWorld().clearPlayerTasks(event.getPlayer().getUniqueId()); 
    }

}
