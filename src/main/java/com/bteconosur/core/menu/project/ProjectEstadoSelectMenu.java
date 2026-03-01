package com.bteconosur.core.menu.project;

import java.util.function.BiConsumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.util.Estado;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProjectEstadoSelectMenu extends Menu {

    private final Estado previousEstado;
    private final BiConsumer<Estado, InventoryClickEvent> onClick;
    private Language language;

    public ProjectEstadoSelectMenu(Player player, String title, Estado previousEstado, Menu previousMenu, @NotNull BiConsumer<Estado, InventoryClickEvent> onClick) {
        super(title, 4, player, previousMenu);
        this.previousEstado = previousEstado;
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

        gui.setItem(2, 2, MenuUtils.getEstadoItem(Estado.ACTIVO, language, previousEstado == Estado.ACTIVO));
        gui.addSlotAction(2, 2, event -> {
            onClick.accept(Estado.ACTIVO, event);
            previousMenu.open();
        });

        gui.setItem(3, 4, MenuUtils.getEstadoItem(Estado.EN_FINALIZACION, language, previousEstado == Estado.EN_FINALIZACION));
        gui.addSlotAction(3, 4, event -> {
            onClick.accept(Estado.EN_FINALIZACION, event);
            previousMenu.open();
        });

        gui.setItem(3,6, MenuUtils.getEstadoItem(Estado.EN_FINALIZACION_EDICION, language, previousEstado == Estado.EN_FINALIZACION_EDICION));
        gui.addSlotAction(3, 6, event -> {
            onClick.accept(Estado.EN_FINALIZACION_EDICION, event);
            previousMenu.open();
        });

        gui.setItem(2,3, MenuUtils.getEstadoItem(Estado.COMPLETADO, language, previousEstado == Estado.COMPLETADO));
        gui.addSlotAction(2, 3, event -> {
            onClick.accept(Estado.COMPLETADO, event);
            previousMenu.open();
        });

        gui.setItem(2,8, MenuUtils.getEstadoItem(Estado.EN_CREACION, language, previousEstado == Estado.EN_CREACION));
        gui.addSlotAction(2, 8, event -> {
            onClick.accept(Estado.EN_CREACION, event);
            previousMenu.open();
        });

        gui.setItem(2, 4, MenuUtils.getEstadoItem(Estado.EDITANDO, language, previousEstado == Estado.EDITANDO));
        gui.addSlotAction(2, 4, event -> {
            onClick.accept(Estado.EDITANDO, event);
            previousMenu.open();
        });

        gui.setItem(2, 6, MenuUtils.getEstadoItem(Estado.REDEFINIENDO, language, previousEstado == Estado.REDEFINIENDO));
        gui.addSlotAction(2, 6, event -> {
            onClick.accept(Estado.REDEFINIENDO, event);
            previousMenu.open();
        });

        gui.setItem(2, 7, MenuUtils.getEstadoItem(Estado.ABANDONADO, language, previousEstado == Estado.ABANDONADO));
        gui.addSlotAction(2, 7, event -> {
            onClick.accept(Estado.ABANDONADO, event);
            previousMenu.open();
        });

        return gui;
    }

}
