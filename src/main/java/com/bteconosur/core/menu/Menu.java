package com.bteconosur.core.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bteconosur.core.util.MenuUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;

public abstract class Menu {
    protected final String title;
    protected final int rows;
    protected final Player player;

    protected BaseGui gui;
    protected Menu previousMenu;

    public Menu(@NotNull String title, @NotNull int rows, @NotNull com.bteconosur.db.model.Player player, @Nullable Menu previousMenu) {
        this.title = title;
        this.rows = rows;
        this.player = player.getBukkitPlayer();
        this.previousMenu = previousMenu;
    }

     public Menu(@NotNull String title, @NotNull int rows, @NotNull com.bteconosur.db.model.Player player) {
        this.title = title;
        this.rows = rows;
        this.player = player.getBukkitPlayer();
    }

    public Menu(@NotNull String title, @NotNull int rows, @NotNull Player bukkitPlayer, @Nullable Menu previousMenu) {
        this.title = title;
        this.rows = rows;
        this.player = bukkitPlayer;
        this.previousMenu = previousMenu;
    }

    public Menu(@NotNull String title, @NotNull int rows, @NotNull Player bukkitPlayer) {
        this.title = title;
        this.rows = rows;
        this.player = bukkitPlayer;
    }

    protected abstract BaseGui createGui();

    public void open() {
        if (player == null || !player.isOnline()) return;
        if (gui == null) {
            gui = createGui();
        }

        if (previousMenu != null) {
            gui.setItem(rows, 1,  MenuUtils.getBackItem());
            gui.addSlotAction(rows, 1, event -> previousMenu.open());
        }

        gui.setItem(rows, 9, MenuUtils.getCloseItem());
        gui.addSlotAction(rows, 9, event -> gui.close(player));      
         
        gui.open(player);
    }   

    public Gui getGui() {
        return (Gui) gui;
    }
}
