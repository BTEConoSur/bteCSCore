package com.bteconosur.core.util;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MenuUtils {

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static GuiItem getBackItem() {
        return buildGuiItem(
            config.getString("items.back.material"),
            config.getString("items.back.name"),
            config.getStringList("items.back.lore")
        );
    }

    public static GuiItem getCloseItem() {
        return buildGuiItem(
            config.getString("items.close.material"),
            config.getString("items.close.name"),
            config.getStringList("items.close.lore")
        );
    }

    public static GuiItem getPreviousPageItem() {
        return buildGuiItem(
            config.getString("items.previous-page.material"),
            config.getString("items.previous-page.name"),
            config.getStringList("items.previous-page.lore")
        );
    }
    
    public static GuiItem getNextPageItem() {
        return buildGuiItem(
            config.getString("items.next-page.material"),
            config.getString("items.next-page.name"),
            config.getStringList("items.next-page.lore")
        );
    }

    public static GuiItem getFillerItem() {
        return buildGuiItem(
            config.getString("items.filler.material"),
            config.getString("items.filler.name"),
            config.getStringList("items.filler.lore")
        );
    }

    public static GuiItem getConfirmItem() {
        return buildGuiItem(
            config.getString("items.confirm.material"),
            config.getString("items.confirm.name"),
            config.getStringList("items.confirm.lore")
        );
    }

    public static GuiItem getCancelItem() {
        return buildGuiItem(
            config.getString("items.cancel.material"),
            config.getString("items.cancel.name"),
            config.getStringList("items.cancel.lore")
        );
    }

    private static GuiItem buildGuiItem(String materialName, String name, List<String> lore) {
        ItemBuilder builder = ItemBuilder.from(getMaterialFromString(materialName))
            .name(MiniMessage.miniMessage().deserialize("<!italic>" + name));
            
        if (lore != null && !lore.isEmpty()) {
            for (String line : lore) {
                builder.lore(MiniMessage.miniMessage().deserialize("<!italic>" + line));
            }
        }

        return builder.asGuiItem();
    }

    private static Material getMaterialFromString(String materialName) {
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }
}
