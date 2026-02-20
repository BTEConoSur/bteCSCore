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

public class ConfirmationMenu extends Menu {

    private final GuiAction<InventoryClickEvent> onConfirm;
    private final GuiAction<InventoryClickEvent> onCancel;

    public ConfirmationMenu(@NotNull String title, @NotNull Player bukkitPlayer, @NotNull Menu previousMenu, @NotNull GuiAction<InventoryClickEvent> onConfirm, @NotNull GuiAction<InventoryClickEvent> onCancel) {
        super(title, 1, bukkitPlayer, previousMenu);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    public ConfirmationMenu(@NotNull String title, @NotNull Player bukkitPlayer, @NotNull Menu previousMenu, @NotNull GuiAction<InventoryClickEvent> onConfirm) {
        super(title, 1, bukkitPlayer, previousMenu);
        this.onConfirm = onConfirm;
        this.onCancel = event -> previousMenu.open();
    }

    public ConfirmationMenu(@NotNull String title, @NotNull com.bteconosur.db.model.Player player, @NotNull Menu previousMenu, @NotNull GuiAction<InventoryClickEvent> onConfirm, @NotNull  GuiAction<InventoryClickEvent> onCancel) {
        super(title, 1, player, previousMenu);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    public ConfirmationMenu(@NotNull String title, @NotNull com.bteconosur.db.model.Player player, @NotNull Menu previousMenu, @NotNull GuiAction<InventoryClickEvent> onConfirm) {
        super(title, 1, player, previousMenu);
        this.onConfirm = onConfirm;
        this.onCancel = event -> previousMenu.open();
    }

    public ConfirmationMenu(@NotNull String title, @NotNull Player bukkitPlayer, @NotNull GuiAction<InventoryClickEvent> onConfirm, @NotNull GuiAction<InventoryClickEvent> onCancel) {
        super(title, 1, bukkitPlayer);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    public ConfirmationMenu(@NotNull String title, @NotNull com.bteconosur.db.model.Player player, @NotNull GuiAction<InventoryClickEvent> onConfirm, @NotNull  GuiAction<InventoryClickEvent> onCancel) {
        super(title, 1, player);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

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

    @Override
    public void open() {
        if (player == null || !player.isOnline()) return;
        if (gui == null) gui = createGui();
         
        gui.open(player);
    }   
    
}
