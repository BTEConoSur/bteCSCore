package com.bteconosur.core.menu.tour;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.menu.HotbarMenu;
import com.bteconosur.core.tour.TourService;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Player;

public class TourHotbarMenu extends HotbarMenu {
    
    private boolean back;

    public TourHotbarMenu(Player player, boolean back) {
        super(player);
        this.back = back;
    }

    @Override
    protected void setupItems() {
        TourService ts = TourService.getInstance();

        setItem(0, MenuUtils.getInfoStopItem(language), p -> {
            ts.sendTourInfo(BTECSPlayer);
        });
        

        if (back) {
            setItem(3, MenuUtils.getBackStopItem(language), p -> {
                ts.previousStop(player.getUniqueId());
            });
        }

        setItem(5, MenuUtils.getNextStopItem(language), p -> {
            ts.nextStop(player.getUniqueId());
        });

        setItem(8, MenuUtils.getStopTourItem(language), p -> {
            ts.stopTour(player.getUniqueId());
        });

    }

    public static void updateBackButton(Language language, UUID playerUuid, boolean back) {
        TourHotbarMenu menu = (TourHotbarMenu) HotbarMenu.getActive(playerUuid);
        if (menu == null) return;
        if (back) {
            menu.setItem(3, MenuUtils.getBackStopItem(language), p -> {
                TourService.getInstance().previousStop(playerUuid);
            });
        } else {
            menu.setItem(3, new ItemStack(Material.AIR), p -> {
            });
        }     
    }

}
