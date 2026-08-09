package com.bteconosur.core.menu.preset;

import java.util.List;

import org.bukkit.Bukkit;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Preset;
import com.bteconosur.db.registry.PlayerRegistry;

import de.rapha149.signgui.SignGUIAction;

import com.bteconosur.db.model.Player;

import dev.triumphteam.gui.guis.GuiItem;

public class PresetListMenu extends PaginatedMenu {

    private boolean isOther = false;
    private Player OtherPlayer = null;

    public PresetListMenu(Player player) {
        super(LanguageHandler.replaceMC("gui-titles.preset-list", player.getLanguage(), player), player);
    }

    public PresetListMenu(Player player, Player otherPlayer) {
        super(LanguageHandler.replaceMC("gui-titles.preset-list", player.getLanguage(), otherPlayer), player);
        this.isOther = true;
        this.OtherPlayer = otherPlayer;
    }

    @Override
    protected void populateItems() {
        Player BTECSPlayer = isOther ? OtherPlayer : super.BTECSPlayer;
        int maxPresets = ConfigHandler.getInstance().getConfig().getInt("preset.max-per-player");
        for (Preset preset : BTECSPlayer.getPresets()) {
            GuiItem item = MenuUtils.getPresetListItem(preset, BTECSPlayer.getLanguage(), isOther);
            item.setAction(event -> {
                event.setCancelled(true);
                if (isOther) {
                    if (super.BTECSPlayer.getPresets().size() >= maxPresets) {
                        PlayerLogger.error(player, LanguageHandler.getText(language, "preset.max-presets").replace("%quantity%", String.valueOf(maxPresets)), (String) null);
                    } else if (super.BTECSPlayer.hasPreset(preset.getId().getNombre())) {
                        PlayerLogger.error(player, LanguageHandler.getText(language, "preset.already").replace("%nombre%", preset.getId().getNombre()), (String) null);
                    } else {
                        PlayerRegistry.getInstance().createPreset(player.getUniqueId(), preset.getBlocksMap(), preset.getId().getNombre());
                        PlayerLogger.info(player, LanguageHandler.getText(language, "preset.copied").replace("%nombre%", preset.getId().getNombre()), (String) null);      
                    }
                    gui.close(player);
                    return;
                }
                PresetCreateMenu menu = new PresetCreateMenu(BTECSPlayer, preset.getId().getNombre(), preset.getBlocksMap(), this);
                menu.open();
            });

            addItem(item);
        }
        if (!isOther) {
            if (BTECSPlayer.getPresets().size() < maxPresets) {
                gui.setItem(6, 5, MenuUtils.getPresetCreateItem(language));
                gui.addSlotAction(6, 5, event -> {
                    event.setCancelled(true);
                    create();
                });
            }
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
