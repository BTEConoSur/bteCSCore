package com.bteconosur.core.menu.project;

import java.util.Set;
import java.util.function.BiConsumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;

import dev.triumphteam.gui.guis.GuiItem;


public class ProjectListMenu extends PaginatedMenu {

    private final Set<Proyecto> proyectos;
    private final BiConsumer<Proyecto, InventoryClickEvent> onClick;

    public ProjectListMenu(Player player, String title, Set<Proyecto> proyectos, @NotNull BiConsumer<Proyecto, InventoryClickEvent> onClick, Menu previousMenu) {
        super(title, player, previousMenu);
        this.onClick = onClick;
        this.proyectos = proyectos;
    }

    public ProjectListMenu(Player player, String title, Set<Proyecto> proyectos, @NotNull BiConsumer<Proyecto, InventoryClickEvent> onClick) {
        super(title, player);
        this.onClick = onClick;
        this.proyectos = proyectos;
    }

    @Override
    protected void populateItems() {
        for (Proyecto proyecto : proyectos) {
            GuiItem item = MenuUtils.getProyecto(proyecto);
            item.setAction(event -> onClick.accept(proyecto, event));
            addItem(item);
        }

    }
}
