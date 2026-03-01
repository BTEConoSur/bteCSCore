package com.bteconosur.core.menu.project;

import java.util.function.BiConsumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PaisSelectMenu extends Menu {

    private final Pais previousPais;
    private final BiConsumer<Pais, InventoryClickEvent> onClick;
    private Language language;

    public PaisSelectMenu(Player player, String title, Pais previousPais, Menu previousMenu, @NotNull BiConsumer<Pais, InventoryClickEvent> onClick) {
        super(title, 3, player, previousMenu);
        this.previousPais = previousPais;
        this.previousMenu = previousMenu;
        this.language = player.getLanguage();
        this.onClick = onClick;
    }

    @Override
    protected BaseGui createGui() {
        Gui gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        gui.getFiller().fill(MenuUtils.getFillerItem());
        PaisRegistry pr = PaisRegistry.getInstance();

        Pais argentina = pr.getArgentina();
        gui.setItem(2, 2, MenuUtils.getArgentinaHeadItem(argentina.equals(previousPais), language));
        gui.addSlotAction(2, 2, event -> {
            onClick.accept(argentina, event);
            previousMenu.open();
        });

        Pais chile = pr.getChile();
        gui.setItem(2, 3, MenuUtils.getChileHeadItem(chile.equals(previousPais), language));
        gui.addSlotAction(2, 3, event -> {
            onClick.accept(chile, event);
            previousMenu.open();
        });

        Pais peru = pr.getPeru();
        gui.setItem(2,4, MenuUtils.getPeruHeadItem(peru.equals(previousPais), language));
        gui.addSlotAction(2, 4, event -> {
            onClick.accept(peru, event);
            previousMenu.open();
        });

        Pais bolivia = pr.getBolivia();
        gui.setItem(2,6, MenuUtils.getBoliviaHeadItem(bolivia.equals(previousPais), language));
        gui.addSlotAction(2, 6, event -> {
            onClick.accept(bolivia, event);
            previousMenu.open();
        });

        Pais uruguay = pr.getUruguay();
        gui.setItem(2,7, MenuUtils.getUruguayHeadItem(uruguay.equals(previousPais), language));
        gui.addSlotAction(2, 7, event -> {
            onClick.accept(uruguay, event);
            previousMenu.open();
        });

        Pais paraguay = pr.getParaguay();
        gui.setItem(2, 8, MenuUtils.getParaguayHeadItem(paraguay.equals(previousPais), language));
        gui.addSlotAction(2, 8, event -> {
            onClick.accept(paraguay, event);
            previousMenu.open();
        });

        return gui;
    }

}
