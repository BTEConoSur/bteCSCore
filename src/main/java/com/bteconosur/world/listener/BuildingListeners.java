package com.bteconosur.world.listener;

import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.world.WorldManager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;


public class BuildingListeners implements Listener {

    private final YamlConfiguration lang;
    private final YamlConfiguration config;
    private final ConsoleLogger logger;

    private final WorldManager worldManager;
    private final DBManager dbManager;

    public BuildingListeners(WorldManager worldManager, DBManager dbManager) {
        this.worldManager = worldManager;
        this.dbManager = dbManager;

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        config = configHandler.getConfig();
        logger = BTEConoSur.getConsoleLogger();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        Player player = dbManager.get(Player.class, event.getPlayer().getUniqueId()); // TODO: Ver si es mejor cachear los jugadores.
        if (player == null) {
            logger.debug("No se econtró el jugador en la base de datos.");
            // Notificación de chat: No se econtró el jugador en la base de datos.
            event.setCancelled(true);
            return;
        }

        if (!worldManager.canBuild(block.getLocation(), player)) {
            event.setCancelled(true);
            // Notificación de chat: No tienes permiso para construir aquí.
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        Player player = dbManager.get(Player.class, event.getPlayer().getUniqueId()); // TODO: Ver si es mejor cachear los jugadores.
        if (player == null) {
            logger.debug("No se econtró el jugador en la base de datos.");
            // Notificación de chat: No se econtró el jugador en la base de datos.
            event.setCancelled(true);
            return;
        }

        if (!worldManager.canBuild(block.getLocation(), player)) {
            event.setCancelled(true);
            // Notificación de chat: No tienes permiso para construir aquí.
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
            && event.getItem() != null && event.getItem().getType().isBlock()) return;

        Player player = dbManager.get(Player.class, event.getPlayer().getUniqueId()); 
        if (player == null) { 
            logger.debug("No se econtró el jugador en la base de datos.");
            // Notificación de chat: No se econtró el jugador en la base de datos.
            event.setCancelled(true); 
            return; 
        }

        Location loc = (event.getClickedBlock() != null) ? event.getClickedBlock().getLocation() : event.getPlayer().getLocation();
        if (!worldManager.canBuild(loc, player)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            // Notificación de chat: No tienes permiso para interactuar aquí.
        }
    }
}
