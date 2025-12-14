package com.bteconosur.core.menu;

import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Player;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public abstract class PaginatedMenu extends Menu {

    PaginatedMenu(String title, Player player) {
        super(title, 6, player);
    }

    PaginatedMenu(String title, Player player, Menu previousMenu) {
        super(title, 6, player, previousMenu);
    }

    PaginatedMenu(String title, org.bukkit.entity.Player bukkitPlayer) {
        super(title, 6, bukkitPlayer);
    }

    PaginatedMenu(String title, org.bukkit.entity.Player bukkitPlayer, Menu previousMenu) {
        super(title, 6, bukkitPlayer, previousMenu);
    }

    @Override
    protected BaseGui createGui() {
        PaginatedGui paginatedGui = Gui.paginated()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .pageSize(36)
            .create();
        
        paginatedGui.getFiller().fillBetweenPoints(rows -1, 1, rows, 9, MenuUtils.getFillerItem());

        paginatedGui.setItem(rows, 3, MenuUtils.getPreviousPageItem());
        paginatedGui.addSlotAction(rows, 3, event -> paginatedGui.previous());

        paginatedGui.setItem(rows, 7, MenuUtils.getNextPageItem());
        paginatedGui.addSlotAction(rows, 7, event -> paginatedGui.next());
        
        this.gui = paginatedGui;
        
        populateItems();

        return paginatedGui;
    }

    protected PaginatedGui getPaginatedGui() {
        return (PaginatedGui) gui;
    }

    protected void openPage(int page) {
        getPaginatedGui().open(player, page);
    }

    protected abstract void populateItems();

}
