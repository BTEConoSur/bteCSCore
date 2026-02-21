package com.bteconosur.world.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldManager;


public class BuildingListeners implements Listener {

    private final WorldManager worldManager;
    private final DBManager dbManager;
    private final PlayerRegistry playerRegistry;

    public BuildingListeners() {
        this.worldManager = WorldManager.getInstance();
        dbManager = DBManager.getInstance();
        playerRegistry = PlayerRegistry.getInstance();
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
            ConsoleLogger.warn(LanguageHandler.getText("console-player-not-registered").replace("%player%", event.getPlayer().getName()));
            event.setCancelled(true); 
            return; 
        }

        //Location loc = (event.getClickedBlock() != null) ? event.getClickedBlock().getLocation() : event.getPlayer().getLocation();
        /*if (!worldManager.canBuild(loc, player)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "no-building-permissions"), (String) null);
        }*/
    }
}
