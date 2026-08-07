package com.bteconosur.core.menu.preset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import de.rapha149.signgui.SignGUIAction;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.components.GuiAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.Menu;

public class PresetCreateMenu extends PaginatedMenu {

    private String presetName;
    private Map<BlockData, Integer> blocks = new LinkedHashMap<>();
    private Map<BlockData, Integer> originalBlocks = new LinkedHashMap<>();
    private boolean isEditing = false;

    public PresetCreateMenu(Player player, String presetName) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.preset-create").replace("%nombre%", presetName), player);
        this.presetName = presetName;
    }

    public PresetCreateMenu(Player player, String presetName, Menu previousMenu) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.preset-create").replace("%nombre%", presetName), player);
        this.presetName = presetName;
        this.previousMenu = previousMenu;
    }

    public PresetCreateMenu(Player player, String presetName, Map<BlockData, Integer> blocks) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.preset-edit").replace("%nombre%", presetName), player);
        this.presetName = presetName;
        this.originalBlocks = new LinkedHashMap<>(blocks);
        this.blocks = blocks;
        this.isEditing = true;
    }

    public PresetCreateMenu(Player player, String presetName, Map<BlockData, Integer> blocks, Menu previousMenu) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.preset-edit").replace("%nombre%", presetName), player);
        this.presetName = presetName;
        this.originalBlocks = new LinkedHashMap<>(blocks);
        this.blocks = blocks;
        this.isEditing = true;
        this.previousMenu = previousMenu;
    }

    @Override
    protected void populateItems() {
        for (Entry<BlockData, Integer> entry : blocks.entrySet()) {
            GuiItem item = MenuUtils.getPresetBlockItem(entry.getKey(), entry.getValue(), language);

            item.setAction(event -> {
                event.setCancelled(true);

                if (event.getClick().isShiftClick()) {
                    blocks.remove(entry.getKey());
                    refreshMenuItems();
                } else if (event.getClick().isLeftClick() || event.getClick().isRightClick()) {
                    editPercentage(entry.getKey());
                }
            });
            gui.addItem(item);
        }

        gui.setPlayerInventoryAction(event -> {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            if (blocks.size() >= 36) return;
            ItemStack item = event.getCurrentItem();
            Material material = item.getType();
            if (!material.isBlock()) return;

            BlockData blockData = Bukkit.createBlockData(material);
            if (blocks.containsKey(blockData)) return;
            blocks.put(blockData, 100);

            GuiItem guiItem = MenuUtils.getPresetBlockItem(blockData, 100, language);
        
            guiItem.setAction(click -> {
                click.setCancelled(true);
                if (event.getClick().isShiftClick()) {
                    blocks.remove(blockData);
                    refreshMenuItems();
                } else if (event.getClick().isLeftClick() || event.getClick().isRightClick()) {
                    editPercentage(blockData);
                }
            });

            gui.addItem(guiItem);
            refreshUI();
            gui.update();
        });

        refreshUI();
        gui.setItem(6,8, MenuUtils.getPresetInfoItem(language));
    }

    private void refreshMenuItems() {
        removePaginatedItems();
        populateItems();
        refreshUI();
        gui.update();
    }

    private void refreshUI() {
        if (isEditing) {
            gui.setItem(6, 7, MenuUtils.getPresetDeleteItem(language));
            gui.addSlotAction(6, 7, event -> {
                event.setCancelled(true);

                GuiAction<InventoryClickEvent> onConfirm = e -> {
                    PlayerRegistry.getInstance().removePreset(player.getUniqueId(), presetName);
                    PlayerLogger.info(player, LanguageHandler.getText(language, "preset.removed").replace("%nombre%", presetName), (String) null);
                    gui.close(player);
                };

                GuiAction<InventoryClickEvent> onCancel = e -> {
                    e.setCancelled(true);
                    this.open();
                };

                String confirmTitle = LanguageHandler.getText(language, "gui-titles.confirm-preset-delete").replace("%nombre%", presetName);
                ConfirmationMenu confirmMenu = new ConfirmationMenu(confirmTitle, player, this, onConfirm, onCancel);
                confirmMenu.open();
            });
        }

        if (!blocks.isEmpty()) {
            gui.setItem(6, 3, MenuUtils.getPresetDeleteAllItem(language));
            gui.addSlotAction(6, 3, event -> {
                event.setCancelled(true);

                GuiAction<InventoryClickEvent> onConfirm = e -> {
                    e.setCancelled(true);
                    blocks.clear();
                    refreshMenuItems();
                    this.open();
                };

                GuiAction<InventoryClickEvent> onCancel = e -> {
                    e.setCancelled(true);
                    this.open();
                };

                String confirmTitle = LanguageHandler.getText(language, "gui-titles.confirm-preset-delete-all");
                ConfirmationMenu confirmMenu = new ConfirmationMenu(confirmTitle, player, this, onConfirm, onCancel);
                confirmMenu.open();
            });
        } else {
            gui.setItem(6, 3, MenuUtils.getFillerItem());
            gui.addSlotAction(6, 3, event -> {
                event.setCancelled(true);
            });
        }

        if (!blocks.equals(originalBlocks) || !isEditing) {
            gui.setItem(6, 5, MenuUtils.getSaveItem(language));
            gui.addSlotAction(6, 5, event -> {
                event.setCancelled(true);
                if (isEditing) {
                    PlayerRegistry.getInstance().editPreset(player.getUniqueId(), blocks, presetName);
                    PlayerLogger.info(player, LanguageHandler.getText(language, "preset.edited").replace("%nombre%", presetName), (String) null);
                } else {
                    PlayerRegistry.getInstance().createPreset(player.getUniqueId(), blocks, presetName);
                    PlayerLogger.info(player, LanguageHandler.getText(language, "preset.created").replace("%nombre%", presetName), (String) null);
                }
                gui.close(player);
            });
        } else {
            gui.setItem(6, 5, MenuUtils.getFillerItem());
            gui.addSlotAction(6, 5, event -> {
                event.setCancelled(true);
            });
        }
        gui.update();
    }

    private void editPercentage(BlockData blockData) {
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String input = result.getLine(0).trim();
            try {
                int percentage = Integer.parseInt(input);
                if (percentage < 0 || percentage > 100) {
                    return List.of(SignGUIAction.run(() -> {
                        Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                            gui.open(player);
                        });
                    }));
                }
                
                Bukkit.getScheduler().runTask(
                    BTEConoSur.getInstance(), () -> {
                        blocks.put(blockData, percentage);
                        refreshMenuItems();
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

        if (!opened) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "internal-error"), (String) null);
        }
    }

}
