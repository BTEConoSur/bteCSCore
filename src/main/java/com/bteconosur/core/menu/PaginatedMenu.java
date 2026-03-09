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

/**
 * Menú paginado que extiende la funcionalidad base de menús.
 * Proporciona navegación entre páginas de items y gestión automática de botones
 * de anterior/siguiente página.
 */
public abstract class PaginatedMenu extends Menu {

    /**
     * Crea un menú paginado a partir de un modelo de jugador.
     *
     * @param title título del menú.
     * @param player modelo de jugador del servidor.
     */
    public PaginatedMenu(@NotNull String title, @NotNull Player player) {
        super(title, 6, player);
    }

    /**
     * Crea un menú paginado a partir de un modelo de jugador con menú anterior.
     *
     * @param title título del menú.
     * @param player modelo de jugador del servidor.
     * @param previousMenu menú anterior para navegación.
     */
    public PaginatedMenu(@NotNull String title, @NotNull Player player, @Nullable Menu previousMenu) {
        super(title, 6, player, previousMenu);
    }

    /**
     * Crea un menú paginado a partir de un jugador de Bukkit.
     *
     * @param title título del menú.
     * @param bukkitPlayer jugador de Bukkit.
     */
    public PaginatedMenu(@NotNull String title, @NotNull org.bukkit.entity.Player bukkitPlayer) {
        super(title, 6, bukkitPlayer);
    }

    /**
     * Crea un menú paginado a partir de un jugador de Bukkit con menú anterior.
     *
     * @param title título del menú.
     * @param bukkitPlayer jugador de Bukkit.
     * @param previousMenu menú anterior para navegación.
     */
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

    /**
     * Agrega un item a la página actual del menú paginado.
     *
     * @param item el item a agregar.
     */
    protected void addItem(GuiItem item) {
        getPaginatedGui().addItem(item);
    }

    /**
     * Elimina todos los items de todas las páginas del menú.
     */
    public void removePaginatedItems() {
        getPaginatedGui().clearPageItems(true);
    }

    /**
     * Obtiene la interfaz paginada actual del menú.
     *
     * @return la interfaz paginada.
     */
    protected PaginatedGui getPaginatedGui() {
        return (PaginatedGui) gui;
    }

    /**
     * Abre el menú en la página especificada.
     *
     * @param page número de página a abrir (1-based).
     */
    protected void openPage(int page) {
        getPaginatedGui().open(player, page);
    }

    /**
     * Actualiza los botones de navegación anterior y siguiente según la página actual.
     */
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

    /**
     * Rellena el menú paginado con los items correspondientes.
     * Este método debe ser implementado por las subclases.
     */
    protected abstract void populateItems();

}
