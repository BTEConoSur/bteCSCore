package com.bteconosur.world.listener;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldManager;


public class BuildingListeners implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
            
        ItemStack item = event.getItem();
        if (item == null) return;
        Player player = PlayerRegistry.getInstance().get(event.getPlayer()); 
        if (player == null) { 
            ConsoleLogger.warn(LanguageHandler.getText("console-player-not-registered").replace("%player%", event.getPlayer().getName()));
            event.setCancelled(true); 
            return; 
        }
        if (PermissionManager.getInstance().isAdmin(player)) return;
            Set<Material> bannedItems = WorldManager.getInstance().getBannedItems();
        if (bannedItems.contains(item.getType())) {
            event.setCancelled(true);
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "item-prohibited").replace("%item%", item.getType().toString()), (String) null);
        }
    }

}
