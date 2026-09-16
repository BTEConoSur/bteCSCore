package com.bteconosur.core.menu.tour;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.tour.TourService;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.registry.TourRegistry;

import dev.triumphteam.gui.guis.GuiItem;

public class TourListMenu extends PaginatedMenu {

    private final Pais pais;
    private final boolean manage;

    private TourParadaListMenu paradaListMenu;

    public TourListMenu(Player player, Menu previousMenu, Pais pais, boolean manage) {
        super(LanguageHandler.replaceMC("gui-titles.tour-list", player.getLanguage(), pais), player, previousMenu);
        this.manage = manage;
        this.pais = pais;
    }

    public TourListMenu(Player player, Pais pais, boolean manage) {
        super(LanguageHandler.replaceMC("gui-titles.tour-list", player.getLanguage(), pais), player);
        this.pais = pais;
        this.manage = manage;
    }

    public TourListMenu(Player player, Menu previousMenu, boolean manage) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.tour-list").replace("%pais.nombrePublico%",
                LanguageHandler.getText(player.getLanguage(), "placeholder.tour.international"))
            , player, previousMenu);
        this.manage = manage;
        this.pais = null;
    }

    public TourListMenu(Player player, boolean manage) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.tour-list").replace("%pais.nombrePublico%",
                LanguageHandler.getText(player.getLanguage(), "placeholder.tour.international"))
            , player);
        this.pais = null;
        this.manage = manage;
    }

    @Override
    protected void populateItems() {
        TourService trs = TourService.getInstance();
        TourRegistry tr = TourRegistry.getInstance();
        if (!manage) {
            GuiItem item2 = MenuUtils.getProyectoTourGuiItem(language);
            item2.setAction(event -> {
                event.setCancelled(true);
                gui.close(player);
                trs.startProjectTour(BTECSPlayer, pais);
            });
            addItem(item2);
        }
        for (Tour tour : tr.getTours(pais != null ? pais.getId() : null)) {
            GuiItem item = MenuUtils.getTourGuiItem(tour, language, manage);
            item.setAction(event -> {
                event.setCancelled(true);
                if (manage) {
                    if (event.getClick().isShiftClick()) {
                        new ConfirmationMenu(LanguageHandler.replaceMC("gui-titles.tour-delete", language, tour), player, this, 
                            event1 -> {
                                tr.delete(tour.getId());
                                gui.close(player);
                            }).open();
                    } else {
                        paradaListMenu = new TourParadaListMenu(BTECSPlayer, this, tour, manage);
                        paradaListMenu.open();
                    }
                } else {
                    if (event.getClick().isLeftClick()) {
                        gui.close(player);
                        trs.startTour(BTECSPlayer, tour);
                    } else {
                        paradaListMenu = new TourParadaListMenu(BTECSPlayer, this, tour, manage);
                        paradaListMenu.open();
                    }     
                }
            });
            addItem(item);
        }
    }

}
