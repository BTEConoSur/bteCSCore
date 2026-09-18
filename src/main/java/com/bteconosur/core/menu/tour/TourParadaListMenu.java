package com.bteconosur.core.menu.tour;

import java.util.List;

import org.bukkit.Bukkit;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.tour.TourService;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.model.TourStop;
import com.bteconosur.db.registry.TourRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

import de.rapha149.signgui.SignGUIAction;
import dev.triumphteam.gui.guis.GuiItem;

public class TourParadaListMenu extends PaginatedMenu {

    private Tour tour;
    private final boolean manage;

    public TourParadaListMenu(Player player, PaginatedMenu previousMenu, Tour tour, boolean manage) {
        super(LanguageHandler.replaceMC("gui-titles.parada-tour" + (manage ? "-manage" : "-list"), player.getLanguage(), tour), player, previousMenu);
        this.tour = tour;
        this.manage = manage;
    }

    public TourParadaListMenu(Player player, Tour tour, boolean manage) {
        super(LanguageHandler.replaceMC("gui-titles.parada-tour" + (manage ? "-manage" : "-list"), player.getLanguage(), tour), player);
        this.tour = tour;
        this.manage = manage;
    }
//TODO: Item cuando es internacional
//TODO: Pais cuando es internacional
    @Override
    protected void populateItems() {
        TourService trs = TourService.getInstance();
        for (TourStop parada : tour.getParadas()) {
            GuiItem item = MenuUtils.getTourStopGuiItem(parada, language, manage);
            item.setAction(event -> {
                event.setCancelled(true);
                if (manage) {
                    if (event.getClick().isShiftClick()) {
                        new ConfirmationMenu(LanguageHandler.replaceMC("gui-titles.parada-tour-delete", language, tour), player, this, 
                            event1 -> {
                                gui.close(player);
                                TourRegistry tr = TourRegistry.getInstance();
                                tr.removeTourParada(tour.getId(), parada.getId().getTourstopId());
                                String message = LanguageHandler.replaceMC("tour.stop.remove-success", language, tour);
                                PlayerLogger.info(player, PlaceholderUtils.replaceMC(message, language, parada), (String) null);
                            }).open();
                    } else if (event.getClick().isLeftClick()) {
                        player.teleport(parada.getLocation());
                        String message = LanguageHandler.replaceMC("tour.stop.tp-success", language, tour);
                        PlayerLogger.info(player, PlaceholderUtils.replaceMC(message, language, parada), (String) null);
                        gui.close(player);
                    } else if (event.getClick().isRightClick()) {
                        editOrden(tour, parada.getId().getTourstopId());
                    }
                } else {
                    if (event.getClick().isRightClick()) {
                        gui.close(player);
                        player.teleport(parada.getLocation());
                        String message = LanguageHandler.replaceMC("tour.stop.tp-success-player", language, tour);
                        PlayerLogger.info(player, PlaceholderUtils.replaceMC(message, language, parada), (String) null);
                    } else if (event.getClick().isLeftClick()) {
                        gui.close(player);
                        trs.startTour(BTECSPlayer, tour, parada.getOrden());
                    }   
                }
            });
            addItem(item);
        }
    }


    private void editOrden(Tour tour, String paradaId) {
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String input = result.getLine(0).trim();
            try {
                int orden = Integer.parseInt(input);
                if (tour.checkOrden(orden)) {
                    return List.of(SignGUIAction.run(() -> {
                        Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                            gui.open(player);
                        });
                    }));
                }
                
                Bukkit.getScheduler().runTask(
                    BTEConoSur.getInstance(), () -> {
                        TourRegistry tr = TourRegistry.getInstance();
                        TourStop parada = tr.editTourParada(tour.getId(), paradaId, orden);
                        String message = LanguageHandler.replaceMC("tour.stop.edit-order-success", language, tour);
                        PlayerLogger.info(player, PlaceholderUtils.replaceMC(message, language, parada), (String) null);
                        gui.close(player);
                    }
                );

            } catch (NumberFormatException ignored) {
            }
            return List.of(SignGUIAction.run(() -> {
                Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                    gui.open(player);
                });
            }));
        }, language);

        if (!opened) PlayerLogger.error(player, LanguageHandler.getText(language, "internal-error"), (String) null);
    }

}
