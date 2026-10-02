package com.bteconosur.core.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class TestHotbarMenu extends HotbarMenu {

    public TestHotbarMenu(Player player) {
        super(player);
    }

    @Override
    protected void setupItems() {
        setItem(0, new ItemStack(Material.DRIPSTONE_BLOCK), p -> {
            p.sendMessage("Teletransportando a casa de akio...");
        });

        setItem(3, new ItemStack(Material.ENDER_PEARL), p -> {
            p.sendMessage("Teletransportando a spawn...");
        });

        setItem(8, new ItemStack(Material.BARRIER), p -> close());
    }
}
