package com.bteconosur.core.menu.preset;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Preset;

import de.rapha149.signgui.SignGUIAction;

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
        int maxPresets = ConfigHandler.getInstance().getConfig().getInt("preset.max-per-player");
        if (BTECSPlayer.getPresets().size() < maxPresets) {
            gui.setItem(6, 5, MenuUtils.getPresetCreateItem(language));
            gui.addSlotAction(6, 5, event -> {
                event.setCancelled(true);
                create();
            });
        }
    }

    private void create() {
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String line1 = result.getLine(0);
            String line2 = result.getLine(1);
            String input = line1 + line2;

            if (input.isBlank() || BTECSPlayer.hasPreset(input)) {
                return List.of(SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        gui.open(player);
                    });
                }));
            }
            
            return List.of(SignGUIAction.run(() -> {
                Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                    PresetCreateMenu menu = new PresetCreateMenu(BTECSPlayer, input, this);
                    menu.open();
                });
            }));
        }, language);

        if (!opened) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "internal-error"), (String) null);
        }
    }

}
