package com.bteconosur.core.menu.tour;

import com.bteconosur.core.menu.HotbarMenu;
import com.bteconosur.core.tour.TourService;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Player;

public class TourHotbarMenu extends HotbarMenu {

    public TourHotbarMenu(Player player) {
        super(player);
    }

    @Override
    protected void setupItems() {
        TourService ts = TourService.getInstance();

        setItem(0, MenuUtils.getInfoStopItem(language), p -> {
            ts.sendTourInfo(BTECSPlayer);
        });

        setItem(3, MenuUtils.getBackStopItem(language), p -> {
            ts.previousStop(player.getUniqueId());
        });

        setItem(5, MenuUtils.getNextStopItem(language), p -> {
            ts.nextStop(player.getUniqueId());
        });

        setItem(8, MenuUtils.getStopTourItem(language), p -> {
            ts.stopTour(player.getUniqueId());
        });

    }

}
