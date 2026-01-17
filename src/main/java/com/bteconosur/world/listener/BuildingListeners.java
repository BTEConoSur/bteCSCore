package com.bteconosur.world.listener;

import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldManager;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;


public class BuildingListeners implements Listener {

    private final WorldManager worldManager;
    private final DBManager dbManager;
    private final PlayerRegistry playerRegistry;
    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public BuildingListeners(WorldManager worldManager) {
        this.worldManager = worldManager;
        dbManager = DBManager.getInstance();
        playerRegistry = PlayerRegistry.getInstance();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        Player player = playerRegistry.get(event.getPlayer().getUniqueId());
        if (player == null) {
            ConsoleLogger.warn("No se encontró al jugador '" + event.getPlayer().getName() + "' en la base de datos.");
            event.setCancelled(true);
            return;
        }

        if (!worldManager.canBuild(block.getLocation(), player)) {
            event.setCancelled(true);
            PlayerLogger.warn(player, lang.getString("no-building-permissions"), (String) null);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        Player player = playerRegistry.get(event.getPlayer().getUniqueId());
        if (player == null) {
            ConsoleLogger.warn("No se encontró al jugador '" + event.getPlayer().getName() + "' en la base de datos.");
            event.setCancelled(true);
            return;
        }

        if (!worldManager.canBuild(block.getLocation(), player)) {
            event.setCancelled(true);
            PlayerLogger.warn(player, lang.getString("no-building-permissions"), (String) null);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR
            && action != Action.PHYSICAL) {
            return;
        }

        if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) 
            && event.getItem() != null && event.getItem().getType().isBlock()) {
                return;
            }

        Player player = dbManager.get(Player.class, event.getPlayer().getUniqueId()); 
        if (player == null) { 
            ConsoleLogger.warn("No se encontró al jugador '" + event.getPlayer().getName() + "' en la base de datos.");
            event.setCancelled(true); 
            return; 
        }

        Location loc = (event.getClickedBlock() != null) ? event.getClickedBlock().getLocation() : event.getPlayer().getLocation();
        if (!worldManager.canBuild(loc, player)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            PlayerLogger.warn(player, lang.getString("no-building-permissions"), (String) null);
        }
    }
}
