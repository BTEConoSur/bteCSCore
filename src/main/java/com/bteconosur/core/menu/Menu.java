package com.bteconosur.core.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.util.MenuUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;

/**
 * Clase base abstracta para la creación de menús de inventario.
 * Define la estructura común de todos los menús, incluyendo título, filas, jugador,
 * idioma y navegación entre menús anteriores.
 */
public abstract class Menu {
    protected final String title;
    protected final int rows;
    protected final Player player;
    private final com.bteconosur.db.model.Player BTECSPlayer;
    protected Language language;

    protected BaseGui gui;
    protected Menu previousMenu;

    /**
     * Crea un menú con menú anterior.
     *
     * @param title título del menú.
     * @param rows número de filas del menú.
     * @param player modelo de jugador del servidor.
     * @param previousMenu menú anterior para navegación, o {@code null} si no hay.
     */
    public Menu(@NotNull String title, @NotNull int rows, @NotNull com.bteconosur.db.model.Player player, @Nullable Menu previousMenu) {
        this.title = title;
        this.rows = rows;
        this.player = player.getBukkitPlayer();
        this.BTECSPlayer = player;
        this.language = player.getLanguage();
        this.previousMenu = previousMenu;
    }

    /**
     * Crea un menú sin menú anterior.
     *
     * @param title título del menú.
     * @param rows número de filas del menú.
     * @param player modelo de jugador del servidor.
     */
    public Menu(@NotNull String title, @NotNull int rows, @NotNull com.bteconosur.db.model.Player player) {
        this.title = title;
        this.rows = rows;
        this.player = player.getBukkitPlayer();
        this.BTECSPlayer = player;
        this.language = player.getLanguage();
    }

    /**
     * Crea un menú a partir de un jugador de Bukkit con menú anterior.
     *
     * @param title título del menú.
     * @param rows número de filas del menú.
     * @param bukkitPlayer jugador de Bukkit.
     * @param previousMenu menú anterior para navegación, o {@code null} si no hay.
     */
    public Menu(@NotNull String title, @NotNull int rows, @NotNull Player bukkitPlayer, @Nullable Menu previousMenu) {
        this.title = title;
        this.rows = rows;
        this.player = bukkitPlayer;
        this.previousMenu = previousMenu;
        this.BTECSPlayer = com.bteconosur.db.model.Player.getBTECSPlayer(bukkitPlayer);
        this.language = BTECSPlayer.getLanguage();
    }

    /**
     * Crea un menú a partir de un jugador de Bukkit sin menú anterior.
     *
     * @param title título del menú.
     * @param rows número de filas del menú.
     * @param bukkitPlayer jugador de Bukkit.
     */
    public Menu(@NotNull String title, @NotNull int rows, @NotNull Player bukkitPlayer) {
        this.title = title;
        this.rows = rows;
        this.player = bukkitPlayer;
        this.BTECSPlayer = com.bteconosur.db.model.Player.getBTECSPlayer(bukkitPlayer);
        this.language = BTECSPlayer.getLanguage();
    }

    /**
     * Crea la interfaz gráfica del menú.
     * Este método debe ser implementado por las subclases.
     *
     * @return la interfaz gráfica creada.
     */
    protected abstract BaseGui createGui();

    /**
     * Abre el menú para el jugador, agregando botones de navegación (anterior, cerrar).
     * Si no existe la interfaz aún, la crea antes de abrirla.
     */
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

    /**
     * Obtiene la interfaz gráfica actual del menú.
     *
     * @return la interfaz de tipo {@code Gui}.
     */
    public Gui getGui() {
        return (Gui) gui;
    }
}
