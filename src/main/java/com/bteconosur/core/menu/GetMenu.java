package com.bteconosur.core.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Player;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
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

        GuiAction<InventoryClickEvent> action = click -> {
            click.setCancelled(false);
        };

        gui.setItem(2, 2, MenuUtils.getLightBlockItem(0));
        gui.addSlotAction(2, 2, action);

        gui.setItem(2, 3, MenuUtils.getLightBlockItem(1));
        gui.addSlotAction(2, 3, action);

        gui.setItem(2, 4, MenuUtils.getLightBlockItem(2));
        gui.addSlotAction(2, 4, action);

        gui.setItem(2, 5, MenuUtils.getLightBlockItem(3));
        gui.addSlotAction(2, 5, action);

        gui.setItem(2, 6, MenuUtils.getLightBlockItem(4));
        gui.addSlotAction(2, 6, action);

        gui.setItem(2, 7, MenuUtils.getLightBlockItem(5));
        gui.addSlotAction(2, 7, action);

        gui.setItem(2, 8, MenuUtils.getLightBlockItem(6));
        gui.addSlotAction(2, 8, action);

        gui.setItem(3, 2, MenuUtils.getLightBlockItem(7));
        gui.addSlotAction(3, 2, action);

        gui.setItem(3, 3, MenuUtils.getLightBlockItem(8));
        gui.addSlotAction(3, 3, action);

        gui.setItem(3, 4, MenuUtils.getLightBlockItem(9));
        gui.addSlotAction(3, 4, action);

        gui.setItem(3, 5, MenuUtils.getLightBlockItem(10));
        gui.addSlotAction(3, 5, action);

        gui.setItem(3, 6, MenuUtils.getLightBlockItem(11));
        gui.addSlotAction(3, 6, action);

        gui.setItem(3, 7, MenuUtils.getLightBlockItem(12));
        gui.addSlotAction(3, 7, action);

        gui.setItem(3, 8, MenuUtils.getLightBlockItem(13));
        gui.addSlotAction(3, 8, action);

        gui.setItem(4, 3, MenuUtils.getLightBlockItem(14));
        gui.addSlotAction(4, 3, action);

        gui.setItem(4, 4, MenuUtils.getLightBlockItem(15));
        gui.addSlotAction(4, 4, action);
        
        gui.setItem(4, 5, ItemBuilder.from(Material.STRUCTURE_VOID).asGuiItem());
        gui.addSlotAction(4, 5, action);

        gui.setItem(4, 6, ItemBuilder.from(Material.BARRIER).asGuiItem());
        gui.addSlotAction(4, 6, action);

        gui.setItem(4, 7, ItemBuilder.from(Material.DEBUG_STICK).asGuiItem());
        gui.addSlotAction(4, 7, action);

        gui.getFiller().fill(MenuUtils.getFillerItem());

        return gui;
    }

}
