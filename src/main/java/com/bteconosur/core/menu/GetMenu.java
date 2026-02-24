package com.bteconosur.core.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Player;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GetMenu extends Menu {

    public GetMenu(Player player, String title) {
        super(title, 5, player);
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        gui.setItem(2, 2, MenuUtils.getLightBlockItem(0));
        gui.addSlotAction(2, 2, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(0));
        });

        gui.setItem(2, 3, MenuUtils.getLightBlockItem(1));
        gui.addSlotAction(2, 3, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(1));
        });
        gui.setItem(2, 4, MenuUtils.getLightBlockItem(2));
        gui.addSlotAction(2, 4, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(2));
        });

        gui.setItem(2, 5, MenuUtils.getLightBlockItem(3));
        gui.addSlotAction(2, 5, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(3));
        });

        gui.setItem(2, 6, MenuUtils.getLightBlockItem(4));
        gui.addSlotAction(2, 6, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(4));
        });

        gui.setItem(2, 7, MenuUtils.getLightBlockItem(5));
        gui.addSlotAction(2, 7, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(5));
        });

        gui.setItem(2, 8, MenuUtils.getLightBlockItem(6));
        gui.addSlotAction(2, 8, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(6));
        });

        gui.setItem(3, 2, MenuUtils.getLightBlockItem(7));
        gui.addSlotAction(3, 2, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(7));
        });

        gui.setItem(3, 3, MenuUtils.getLightBlockItem(8));
        gui.addSlotAction(3, 3, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(8));
        });

        gui.setItem(3, 4, MenuUtils.getLightBlockItem(9));
        gui.addSlotAction(3, 4, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(9));
        }); 

        gui.setItem(3, 5, MenuUtils.getLightBlockItem(10));
        gui.addSlotAction(3, 5, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(10));
        }); 

        gui.setItem(3, 6, MenuUtils.getLightBlockItem(11));
        gui.addSlotAction(3, 6, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(11));
        });

        gui.setItem(3, 7, MenuUtils.getLightBlockItem(12));
        gui.addSlotAction(3, 7, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(12));
        });

        gui.setItem(3, 8, MenuUtils.getLightBlockItem(13));
        gui.addSlotAction(3, 8, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(13));
        });

        gui.setItem(4, 3, MenuUtils.getLightBlockItem(14));
        gui.addSlotAction(4, 3, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(14));
        });

        gui.setItem(4, 4, MenuUtils.getLightBlockItem(15));
        gui.addSlotAction(4, 4, click -> {
            player.getInventory().addItem(MenuUtils.getLightBlockItemStack(15));
        });
        
        gui.setItem(4, 5, ItemBuilder.from(Material.STRUCTURE_VOID).asGuiItem());
        gui.addSlotAction(4, 5, click -> {
            player.getInventory().addItem(ItemStack.of(Material.STRUCTURE_VOID));
        });

        gui.setItem(4, 6, ItemBuilder.from(Material.BARRIER).asGuiItem());
        gui.addSlotAction(4, 6, click -> {
            player.getInventory().addItem(ItemStack.of(Material.BARRIER));
        });

        gui.setItem(4, 7, ItemBuilder.from(Material.DEBUG_STICK).asGuiItem());
        gui.addSlotAction(4, 7, click -> {
            player.getInventory().addItem(ItemStack.of(Material.DEBUG_STICK));
        });

        gui.getFiller().fill(MenuUtils.getFillerItem());

        return gui;
    }

}
