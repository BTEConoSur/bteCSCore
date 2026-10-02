package com.bteconosur.core.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.Event.Result;

import com.bteconosur.core.menu.HotbarMenu;

import io.papermc.paper.event.player.PlayerPickItemEvent;

public class HotbarListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        HotbarMenu menu = HotbarMenu.getActive(player);
        if (menu == null) return;

        event.setCancelled(true);
        event.setUseItemInHand(Result.DENY); //TODO: Ver si se puede arreglar lo de las perlas
        
        int slot = player.getInventory().getHeldItemSlot();
        menu.handleAction(slot);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            if (HotbarMenu.getActive(player) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickItem(PlayerPickItemEvent event) {
        if (HotbarMenu.getActive(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (HotbarMenu.getActive(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (HotbarMenu.getActive(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (HotbarMenu.getActive(player) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCreativeInventoryClick(InventoryCreativeEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (HotbarMenu.getActive(player) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (HotbarMenu.getActive(player) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (HotbarMenu.getActive(player) != null) {
                event.setCancelled(true);
            }
        }
    }

}
