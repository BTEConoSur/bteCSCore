package com.bteconosur.core.menu;

import java.util.List;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TerraUtils;
import com.bteconosur.core.util.json.RealLocation;
import com.bteconosur.db.model.Player;
import com.bteconosur.world.WorldManager;

import dev.triumphteam.gui.guis.GuiItem;

public class TpdirMenu extends PaginatedMenu {

    private List<RealLocation> locations;

    public TpdirMenu(Player player, String title, List<RealLocation> locations) {
        super(title, player);
        this.locations = locations;
    }

    @Override
    protected void populateItems() {
        for (RealLocation loc : locations) {
            double[] coords = TerraUtils.toMc(loc.lat, loc.lon);
            if (!WorldManager.getInstance().getBTEWorld().checkPaisMove(coords[0], coords[1])) continue;

            GuiItem item = MenuUtils.getTpdirItem(loc.displayName);
            item.setAction(event -> {
                PlayerLogger.info(player, LanguageHandler.getText(language, "tpdir.tped").replace("%destino%", loc.displayName), (String) null);
                player.performCommand("terraplusminus:tpll " + loc.lat + " " + loc.lon);
                player.closeInventory();
            });
            gui.addItem(item);
        }
       
    }

}
