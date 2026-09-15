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
    private boolean last;

    public TourHotbarMenu(Player player, boolean back, boolean last) {
        super(player);
        this.back = back;
        this.last = last;
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

            setItem(4, MenuUtils.getFirstStopItem(language), p -> {
                ts.goFirst(player.getUniqueId());
            });
        }

        if (last) {
            setItem(5, MenuUtils.getLastStopItem(language), p -> {
                ts.stopTour(player.getUniqueId());
            });
        } else {
            setItem(5, MenuUtils.getNextStopItem(language), p -> {
                ts.nextStop(player.getUniqueId());
            });
        }

        setItem(8, MenuUtils.getStopTourItem(language), p -> {
            ts.stopTour(player.getUniqueId());
        });

    }

    public static void updateBackButton(Language language, UUID playerUuid, boolean back) {
        TourHotbarMenu menu = (TourHotbarMenu) HotbarMenu.getActive(playerUuid);
        if (menu == null) return;
        if (back) {
            TourService ts = TourService.getInstance();
            menu.setItem(3, MenuUtils.getBackStopItem(language), p -> {
                ts.previousStop(playerUuid);
            });
            menu.setItem(4, MenuUtils.getFirstStopItem(language), p -> {
                ts.goFirst(playerUuid);
            });
        } else {
            menu.setItem(3, new ItemStack(Material.AIR), p -> {});
            menu.setItem(4, new ItemStack(Material.AIR), p -> {});
        }     
    }

    public static void updateLastButton(Language language, UUID playerUuid, boolean last) {
        TourHotbarMenu menu = (TourHotbarMenu) HotbarMenu.getActive(playerUuid);
        if (menu == null) return;
        TourService ts = TourService.getInstance();
        if (last) {
            menu.setItem(5, MenuUtils.getLastStopItem(language), p -> {
                ts.stopTour(playerUuid);
            });
        } else {
            menu.setItem(5, MenuUtils.getNextStopItem(language), p -> {
                ts.nextStop(playerUuid);
            });
        }
    }

}
