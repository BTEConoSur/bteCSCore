package com.bteconosur.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.util.MenuUtils;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Menú de confirmación que presenta dos opciones: confirmar o cancelar.
 * Utiliza una interfaz de tipo HOPPER (5 slots) para mostrar botones de acción.
 */
public class ConfirmationMenu extends Menu {

    private final GuiAction<InventoryClickEvent> onConfirm;
    private final GuiAction<InventoryClickEvent> onCancel;

    /**
     * Crea un menú de confirmación con acciones personalizadas de confirm/cancel y menú anterior.
     *
     * @param title título del menú.
     * @param bukkitPlayer jugador de Bukkit.
     * @param previousMenu menú anterior para retroceder.
     * @param onConfirm acción ejecutada al hacer clic en confirmar.
     * @param onCancel acción ejecutada al hacer clic en cancelar.
     */
    public ConfirmationMenu(@NotNull String title, @NotNull Player bukkitPlayer, @NotNull Menu previousMenu, @NotNull GuiAction<InventoryClickEvent> onConfirm, @NotNull GuiAction<InventoryClickEvent> onCancel) {
        super(title, 1, bukkitPlayer, previousMenu);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    /**
     * Crea un menú de confirmación con action de confirm y retorno al menú anterior en cancel.
     *
     * @param title título del menú.
     * @param bukkitPlayer jugador de Bukkit.
     * @param previousMenu menú anterior para retroceder en cancelación.
     * @param onConfirm acción ejecutada al hacer clic en confirmar.
     */
    public ConfirmationMenu(@NotNull String title, @NotNull Player bukkitPlayer, @NotNull Menu previousMenu, @NotNull GuiAction<InventoryClickEvent> onConfirm) {
        super(title, 1, bukkitPlayer, previousMenu);
        this.onConfirm = onConfirm;
        this.onCancel = event -> previousMenu.open();
    }

    /**
     * Crea un menú de confirmación con modelo de jugador y acciones personalizadas.
     *
     * @param title título del menú.
     * @param player modelo de jugador del servidor.
     * @param previousMenu menú anterior para retroceder.
     * @param onConfirm acción ejecutada al hacer clic en confirmar.
     * @param onCancel acción ejecutada al hacer clic en cancelar.
     */
    public ConfirmationMenu(@NotNull String title, @NotNull com.bteconosur.db.model.Player player, @NotNull Menu previousMenu, @NotNull GuiAction<InventoryClickEvent> onConfirm, @NotNull  GuiAction<InventoryClickEvent> onCancel) {
        super(title, 1, player, previousMenu);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    /**
     * Crea un menú de confirmación con modelo de jugador y action confirm, retorno en cancel.
     *
     * @param title título del menú.
     * @param player modelo de jugador del servidor.
     * @param previousMenu menú anterior para retroceder en cancelación.
     * @param onConfirm acción ejecutada al hacer clic en confirmar.
     */
    public ConfirmationMenu(@NotNull String title, @NotNull com.bteconosur.db.model.Player player, @NotNull Menu previousMenu, @NotNull GuiAction<InventoryClickEvent> onConfirm) {
        super(title, 1, player, previousMenu);
        this.onConfirm = onConfirm;
        this.onCancel = event -> previousMenu.open();
    }

    /**
     * Crea un menú de confirmación sin menú anterior con acciones personalizadas.
     *
     * @param title título del menú.
     * @param bukkitPlayer jugador de Bukkit.
     * @param onConfirm acción ejecutada al hacer clic en confirmar.
     * @param onCancel acción ejecutada al hacer clic en cancelar.
     */
    public ConfirmationMenu(@NotNull String title, @NotNull Player bukkitPlayer, @NotNull GuiAction<InventoryClickEvent> onConfirm, @NotNull GuiAction<InventoryClickEvent> onCancel) {
        super(title, 1, bukkitPlayer);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    /**
     * Crea un menú de confirmación con modelo de jugador sin menú anterior.
     *
     * @param title título del menú.
     * @param player modelo de jugador del servidor.
     * @param onConfirm acción ejecutada al hacer clic en confirmar.
     * @param onCancel acción ejecutada al hacer clic en cancelar.
     */
    public ConfirmationMenu(@NotNull String title, @NotNull com.bteconosur.db.model.Player player, @NotNull GuiAction<InventoryClickEvent> onConfirm, @NotNull  GuiAction<InventoryClickEvent> onCancel) {
        super(title, 1, player);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    /**
     * Crea la interfaz gráfica del menú de confirmación con botones de confirmar y cancelar.
     *
     * @return la interfaz gráfica creada.
     */
    @Override
    protected BaseGui createGui() {
        Gui gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .type(GuiType.HOPPER)
            .disableAllInteractions()
            .create();
        Language language = com.bteconosur.db.model.Player.getBTECSPlayer(player).getLanguage();
        gui.getFiller().fill(MenuUtils.getFillerItem());

        gui.setItem(4, MenuUtils.getConfirmItem(language));
        gui.addSlotAction(4, onConfirm);

        gui.setItem(0, MenuUtils.getCancelItem(language));
        gui.addSlotAction(0, onCancel);
        
        return gui;
    }

    /**
     * Abre el menú de confirmación para el jugador si está en línea.
     * Override para evitar agregar botones de navegación.
     */
    @Override
    public void open() {
        if (player == null || !player.isOnline()) return;
        if (gui == null) gui = createGui();
         
        gui.open(player);
    }   
    
}
