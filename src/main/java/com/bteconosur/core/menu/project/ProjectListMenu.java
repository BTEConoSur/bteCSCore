package com.bteconosur.core.menu.project;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;

import de.rapha149.signgui.SignGUIAction;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;


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
        PaginatedGui gui = getPaginatedGui();

        gui.setItem(rows, 8, MenuUtils.getSearchItem("Nombre", null));
        gui.addSlotAction(rows, 8, event -> {
            searchByName();
        });

        gui.setItem(rows, 7, MenuUtils.getSearchItem("ID", null));
        gui.addSlotAction(rows, 7, event -> {
            searchById();
        });
    }

    private void searchByName() {  
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String line1 = result.getLine(0);
            String line2 = result.getLine(1);
            String search = line1 + line2;
            if (search.isBlank()) return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        removePaginatedItems();
                        for (Proyecto proyecto : proyectos) {
                            GuiItem item = MenuUtils.getProyecto(proyecto);
                            item.setAction(event -> onClick.accept(proyecto, event));
                            addItem(item);
                        }
                        setSearchItemsAndOpen(null, null);
                    });
                })
            );
            return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        removePaginatedItems();
                        for (Proyecto proyecto : proyectos) {
                            String nombre = proyecto.getNombre() != null ? proyecto.getNombre() : "";
                            if (!nombre.toLowerCase().contains(search.toLowerCase())) continue;
                            GuiItem item = MenuUtils.getProyecto(proyecto);
                            item.setAction(event -> onClick.accept(proyecto, event));
                            addItem(item);
                        }
                        setSearchItemsAndOpen(search, null);
                    });
                })
            );
        });
        YamlConfiguration lang = ConfigHandler.getInstance().getLang();
        if (!opened) PlayerLogger.error(Player.getBTECSPlayer(player), lang.getString("internal-error"), (String) null);
    }

    private void searchById() {  
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String line1 = result.getLine(0);
            String line2 = result.getLine(1);
            String search = line1 + line2;
            if (search.isBlank()) return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        removePaginatedItems();
                        for (Proyecto proyecto : proyectos) {
                            GuiItem item = MenuUtils.getProyecto(proyecto);
                            item.setAction(event -> onClick.accept(proyecto, event));
                            addItem(item);
                        }
                        setSearchItemsAndOpen(null, null);
                    });
                })
            );
            return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        removePaginatedItems();
                        for (Proyecto proyecto : proyectos) {
                            if (!proyecto.getId().toLowerCase().contains(search.toLowerCase())) continue;
                            GuiItem item = MenuUtils.getProyecto(proyecto);
                            item.setAction(event -> onClick.accept(proyecto, event));
                            addItem(item);
                        }
                        setSearchItemsAndOpen(null, search);
                    });
                })
            );
        });
        YamlConfiguration lang = ConfigHandler.getInstance().getLang();
        if (!opened) PlayerLogger.error(Player.getBTECSPlayer(player), lang.getString("internal-error"), (String) null);
    }

    private void setSearchItemsAndOpen(String nombreSearch, String idSearch) {
        PaginatedGui gui = getPaginatedGui();
        gui.updateItem(rows, 8, MenuUtils.getSearchItem("Nombre", nombreSearch));
        gui.updateItem(rows, 7, MenuUtils.getSearchItem("ID", idSearch));
        gui.update();
        gui.open(player);
    }
}
