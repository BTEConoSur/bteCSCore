package com.bteconosur.core.menu.project;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.Estado;

import de.rapha149.signgui.SignGUIAction;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;


public class ProjectListMenu extends PaginatedMenu {

    private final LinkedHashSet<Proyecto> proyectos;
    private final BiConsumer<Proyecto, InventoryClickEvent> onClick;
    private Language language;

    private final Player BTECSPlayer;
    private Estado selectedEstado = null;
    private Pais selectedPais = null;

    private ProjectEstadoSelectMenu estadoSelectMenu;
    private PaisSelectMenu paisSelectMenu;

    public ProjectListMenu(Player player, String title, LinkedHashSet<Proyecto> proyectos, @NotNull BiConsumer<Proyecto, InventoryClickEvent> onClick, Menu previousMenu) {
        super(title, player, previousMenu);
        this.onClick = onClick;
        this.proyectos = proyectos;
        this.language = player.getLanguage();
        this.BTECSPlayer = player;
    }

    public ProjectListMenu(Player player, String title, LinkedHashSet<Proyecto> proyectos, @NotNull BiConsumer<Proyecto, InventoryClickEvent> onClick) {
        super(title, player);
        this.onClick = onClick;
        this.proyectos = proyectos;
        this.language = player.getLanguage();
        this.BTECSPlayer = player;
    }

    public ProjectListMenu(Player player, String title, Set<Proyecto> proyectos, @NotNull BiConsumer<Proyecto, InventoryClickEvent> onClick, Menu previousMenu) {
        super(title, player, previousMenu);
        this.onClick = onClick;
        this.proyectos = new LinkedHashSet<>(proyectos);
        this.language = player.getLanguage();
        this.BTECSPlayer = player;
    }

    public ProjectListMenu(Player player, String title, Set<Proyecto> proyectos, @NotNull BiConsumer<Proyecto, InventoryClickEvent> onClick) {
        super(title, player);
        this.onClick = onClick;
        this.proyectos = new LinkedHashSet<>(proyectos);
        this.language = player.getLanguage();
        this.BTECSPlayer = player;
    }

    @Override
    protected void populateItems() {
        for (Proyecto proyecto : proyectos) {
            GuiItem item = MenuUtils.getProyecto(proyecto, language);
            item.setAction(event -> onClick.accept(proyecto, event));
            addItem(item);
        }
        PaginatedGui gui = getPaginatedGui();

        gui.setItem(rows, 8, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.nombre"), null, language));
        gui.addSlotAction(rows, 8, event -> {
            searchByName();
        });

        gui.setItem(rows, 7, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.id"), null, language));
        gui.addSlotAction(rows, 7, event -> {
            searchById();
        });

        gui.setItem(rows, 2, MenuUtils.getSearchItem(selectedPais, language));
        gui.addSlotAction(rows, 2, event -> {
            searchByPais();
        });

        gui.setItem(rows, 3, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.ubicacion"), null, language));
        gui.addSlotAction(rows, 3, event -> {
            searchByUbication();
        });

        gui.setItem(rows, 5,MenuUtils.getSearchItem(selectedEstado, language));
        gui.addSlotAction(rows, 5, event -> {
            searchByEstado();
        });
    }

    private void searchByEstado() {  
        estadoSelectMenu = new ProjectEstadoSelectMenu(BTECSPlayer, LanguageHandler.getText(language, "gui-titles.search-estado"), selectedEstado, this, (estado, event) -> {
            if (estado == selectedEstado) {
                selectedEstado = null;
                resetSearch();
                return;
            }
            selectedEstado = estado;
            removePaginatedItems();
            for (Proyecto proyecto : proyectos) {
                if (proyecto.getEstado() != estado) continue;
                GuiItem item = MenuUtils.getProyecto(proyecto, language);
                item.setAction(e -> onClick.accept(proyecto, e));
                addItem(item);
            }
            setSearchItemsAndOpen(null, null, null, null, estado);
        });
        estadoSelectMenu.open();
    }

    private void searchByUbication() {  
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String line1 = result.getLine(0);
            String line2 = result.getLine(1);
            String search = line1 + line2;
            String normalizedSearch = Normalizer.normalize(search.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            String[] tokens = normalizedSearch.split(" ");
            if (search.isBlank()) return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        resetSearch();
                    });
                })
            );
            return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        removePaginatedItems();
                        for (Proyecto proyecto : proyectos) {
                            String ubicacion = proyecto.getDivision().getSearchIndex();
                            boolean matches = true;
                            for (String token : tokens) {
                                if (!ubicacion.contains(token)) {
                                    matches = false;
                                    break;
                                }
                            }
                            if (!matches) continue;
                            GuiItem item = MenuUtils.getProyecto(proyecto, language);
                            item.setAction(event -> onClick.accept(proyecto, event));
                            addItem(item);
                        }
                        setSearchItemsAndOpen(null, null, null, search, null);
                    });
                })
            );
        }, language);
        if (!opened) PlayerLogger.error(Player.getBTECSPlayer(player), LanguageHandler.getText(language,"internal-error"), (String) null);
    }

    private void searchByPais() {  
        paisSelectMenu = new PaisSelectMenu(BTECSPlayer, LanguageHandler.getText(language, "gui-titles.search-pais"), selectedPais, this, (pais, event) -> {
            if (pais == selectedPais) {
                selectedPais = null;
                resetSearch();
                return;
            }
            selectedPais = pais;
            removePaginatedItems();
            for (Proyecto proyecto : proyectos) {
                if (!proyecto.getPais().equals(pais)) continue;
                GuiItem item = MenuUtils.getProyecto(proyecto, language);
                item.setAction(e -> onClick.accept(proyecto, e));
                addItem(item);
            }
            setSearchItemsAndOpen(null, null, selectedPais, null, null);
        });
        paisSelectMenu.open();
    }

    private void searchByName() {  
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String line1 = result.getLine(0);
            String line2 = result.getLine(1);
            String search = line1 + line2;
            if (search.isBlank()) return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        resetSearch();
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
                            GuiItem item = MenuUtils.getProyecto(proyecto, language);
                            item.setAction(event -> onClick.accept(proyecto, event));
                            addItem(item);
                        }
                        setSearchItemsAndOpen(search, null, null, null, null);
                    });
                })
            );
        }, language);
        if (!opened) PlayerLogger.error(Player.getBTECSPlayer(player), LanguageHandler.getText(language,"internal-error"), (String) null);
    }

    private void searchById() {  
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String line1 = result.getLine(0);
            String line2 = result.getLine(1);
            String search = line1 + line2;
            if (search.isBlank()) return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        resetSearch();
                    });
                })
            );
            return List.of(
                SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        removePaginatedItems();
                        for (Proyecto proyecto : proyectos) {
                            if (!proyecto.getId().toLowerCase().contains(search.toLowerCase())) continue;
                            GuiItem item = MenuUtils.getProyecto(proyecto, language);
                            item.setAction(event -> onClick.accept(proyecto, event));
                            addItem(item);
                        }
                        setSearchItemsAndOpen(null, search, null, null, null);
                    });
                })
            );
        }, language);
        if (!opened) PlayerLogger.error(Player.getBTECSPlayer(player), LanguageHandler.getText(language,"internal-error"), (String) null);
    }

    private void resetSearch() {
        removePaginatedItems();
        for (Proyecto proyecto : proyectos) {
            GuiItem item = MenuUtils.getProyecto(proyecto, language);
            item.setAction(event -> onClick.accept(proyecto, event));
            addItem(item);
        }
        setSearchItemsAndOpen(null, null, null, null, null);
    }

    private void setSearchItemsAndOpen(String nombreSearch, String idSearch, Pais paisSearch, String ubicacionSearch, Estado estado) {
        PaginatedGui gui = getPaginatedGui();
        gui.updateItem(rows, 8, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.nombre"), nombreSearch, language));
        gui.updateItem(rows, 7, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.id"), idSearch, language));
        gui.updateItem(rows, 2, MenuUtils.getSearchItem(paisSearch, language));
        gui.updateItem(rows, 3, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.ubicacion"), ubicacionSearch, language));
        gui.updateItem(rows, 5, MenuUtils.getSearchItem(estado, language));
        gui.open(player);
    }
}
