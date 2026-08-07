package com.bteconosur.core.menu.preset;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Preset;
import com.bteconosur.db.model.Player;

import dev.triumphteam.gui.guis.GuiItem;

public class PresetListMenu extends PaginatedMenu {

    public PresetListMenu(Player player) {
        super(LanguageHandler.replaceMC("gui-titles.preset-list", player.getLanguage(), player), player);
    }

    @Override
    protected void populateItems() {
        for (Preset preset : BTECSPlayer.getPresets()) {
            GuiItem item = MenuUtils.getPresetListItem(preset, BTECSPlayer.getLanguage());
            item.setAction(event -> {
                event.setCancelled(true);
                PresetCreateMenu menu = new PresetCreateMenu(BTECSPlayer, preset.getId().getNombre(), preset.getBlocksMap(), this);
                menu.open();
            });

            addItem(item);
        }
    }

}
