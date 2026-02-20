package com.bteconosur.core.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.util.MenuUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;

public abstract class Menu {
    protected final String title;
    protected final int rows;
    protected final Player player;
    private final com.bteconosur.db.model.Player BTECSPlayer;
    protected Language language;

    protected BaseGui gui;
    protected Menu previousMenu;

    public Menu(@NotNull String title, @NotNull int rows, @NotNull com.bteconosur.db.model.Player player, @Nullable Menu previousMenu) {
        this.title = title;
        this.rows = rows;
        this.player = player.getBukkitPlayer();
        this.BTECSPlayer = player;
        this.language = player.getLanguage();
        this.previousMenu = previousMenu;
    }

     public Menu(@NotNull String title, @NotNull int rows, @NotNull com.bteconosur.db.model.Player player) {
        this.title = title;
        this.rows = rows;
        this.player = player.getBukkitPlayer();
        this.BTECSPlayer = player;
        this.language = player.getLanguage();
    }

    public Menu(@NotNull String title, @NotNull int rows, @NotNull Player bukkitPlayer, @Nullable Menu previousMenu) {
        this.title = title;
        this.rows = rows;
        this.player = bukkitPlayer;
        this.previousMenu = previousMenu;
        this.BTECSPlayer = com.bteconosur.db.model.Player.getBTECSPlayer(bukkitPlayer);
        this.language = BTECSPlayer.getLanguage();
    }

    public Menu(@NotNull String title, @NotNull int rows, @NotNull Player bukkitPlayer) {
        this.title = title;
        this.rows = rows;
        this.player = bukkitPlayer;
        this.BTECSPlayer = com.bteconosur.db.model.Player.getBTECSPlayer(bukkitPlayer);
        this.language = BTECSPlayer.getLanguage();
    }

    protected abstract BaseGui createGui();

    public void open() {
        if (player == null || !player.isOnline()) return;
        if (gui == null) gui = createGui();

        if (previousMenu != null) {
            gui.setItem(rows, 1,  MenuUtils.getBackItem(language));
            gui.addSlotAction(rows, 1, event -> previousMenu.open());
        }

        gui.setItem(rows, 9, MenuUtils.getCloseItem(language));
        gui.addSlotAction(rows, 9, event -> gui.close(player));      
         
        gui.open(player);
    }   

    public Gui getGui() {
        return (Gui) gui;
    }
}
