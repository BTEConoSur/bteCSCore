package com.bteconosur.core.menu;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Player;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public abstract class PaginatedMenu extends Menu {

    public PaginatedMenu(@NotNull String title, @NotNull Player player) {
        super(title, 6, player);
    }

    public PaginatedMenu(@NotNull String title, @NotNull Player player, @Nullable Menu previousMenu) {
        super(title, 6, player, previousMenu);
    }

    public PaginatedMenu(@NotNull String title, @NotNull org.bukkit.entity.Player bukkitPlayer) {
        super(title, 6, bukkitPlayer);
    }

    public PaginatedMenu(@NotNull String title, @NotNull org.bukkit.entity.Player bukkitPlayer, @Nullable Menu previousMenu) {
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
        
        paginatedGui.getFiller().fillBetweenPoints(rows - 1, 1, rows, 9, MenuUtils.getFillerItem());

        paginatedGui.setItem(rows, 4, MenuUtils.getPreviousPageItem(language));
        paginatedGui.addSlotAction(rows, 4, event -> {
            if (paginatedGui.getCurrentPageNum() > 1) {
                paginatedGui.previous();
                updateNavigationButtons();
            }
        });

        paginatedGui.setItem(rows, 6, MenuUtils.getNextPageItem(language));
        paginatedGui.addSlotAction(rows, 6, event -> {
            if (paginatedGui.getCurrentPageNum() < paginatedGui.getPagesNum()) {
                paginatedGui.next();
                updateNavigationButtons();
            }
        });
        
        paginatedGui.setOpenGuiAction(event -> updateNavigationButtons());
        
        this.gui = paginatedGui;
        
        populateItems();

        return paginatedGui;
    }

    protected void addItem(GuiItem item) {
        getPaginatedGui().addItem(item);
    }

    public void removePaginatedItems() {
        getPaginatedGui().clearPageItems(true);
    }

    protected PaginatedGui getPaginatedGui() {
        return (PaginatedGui) gui;
    }

    protected void openPage(int page) {
        getPaginatedGui().open(player, page);
    }

    private void updateNavigationButtons() {
        PaginatedGui paginatedGui = getPaginatedGui();
        
        if (paginatedGui.getCurrentPageNum() > 1) {
            paginatedGui.updateItem(rows, 4, MenuUtils.getPreviousPageItem(language));
        } else {
            paginatedGui.updateItem(rows, 4, MenuUtils.getFillerItem());
        }
        
        if (paginatedGui.getCurrentPageNum() < paginatedGui.getPagesNum()) {
            paginatedGui.updateItem(rows, 6, MenuUtils.getNextPageItem(language));
        } else {
            paginatedGui.updateItem(rows, 6, MenuUtils.getFillerItem());
        }
    }

    protected abstract void populateItems();

}
