package com.bteconosur.core.menu.tour;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TourPaisMenu extends Menu {

    private boolean manage;
    private TourListMenu tourListMenu;

    public TourPaisMenu(Player player, boolean manage) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.tour-pais-select"), 4, player);
        this.manage = manage;
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        gui.getFiller().fill(MenuUtils.getFillerItem());
        
        PaisRegistry pr = PaisRegistry.getInstance();
        Pais arg = pr.getArgentina();
        gui.setItem(3,2, MenuUtils.getArgentinaHeadItem(false, language));
        gui.addSlotAction(3,2, event -> {
            tourListMenu = new TourListMenu(BTECSPlayer, this, arg, manage);
            tourListMenu.open();
        });

        Pais chile = pr.getChile();
        gui.setItem(3,3, MenuUtils.getChileHeadItem(false, language));
        gui.addSlotAction(3,3, event -> {
            tourListMenu = new TourListMenu(BTECSPlayer, this, chile, manage);
            tourListMenu.open();
        });

        Pais peru = pr.getPeru();
        gui.setItem(3,4, MenuUtils.getPeruHeadItem(false, language));
        gui.addSlotAction(3,4, event -> {
            tourListMenu = new TourListMenu(BTECSPlayer, this, peru, manage);
            tourListMenu.open();
        });

        gui.setItem(2,5, MenuUtils.getConosurHeadItem(language));
        gui.addSlotAction(2,5, event -> {
            tourListMenu = new TourListMenu(BTECSPlayer, this, manage);
            tourListMenu.open();
        });

        Pais antartida = pr.getAntartida();
        gui.setItem(3,5, MenuUtils.getAntartidaHeadItem(language));
        gui.addSlotAction(3,5, event -> {
            tourListMenu = new TourListMenu(BTECSPlayer, this, antartida, manage);
            tourListMenu.open();
        });

        Pais bolivia = pr.getBolivia();
        gui.setItem(3,6, MenuUtils.getBoliviaHeadItem(false, language));
        gui.addSlotAction(3,6, event -> {
            tourListMenu = new TourListMenu(BTECSPlayer, this, bolivia, manage);
            tourListMenu.open();
        });

        Pais uruguay = pr.getUruguay();
        gui.setItem(3,7, MenuUtils.getUruguayHeadItem(false, language));
        gui.addSlotAction(3,7, event -> {
            tourListMenu = new TourListMenu(BTECSPlayer, this, uruguay, manage);
            tourListMenu.open();
        });

        Pais paraguay = pr.getParaguay();
        gui.setItem(3,8, MenuUtils.getParaguayHeadItem(false, language));
        gui.addSlotAction(3,8, event -> {
            tourListMenu = new TourListMenu(BTECSPlayer, this, paraguay, manage);
            tourListMenu.open();
        });
        
        return gui;
    }

}
