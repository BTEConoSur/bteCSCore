package com.bteconosur.core.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MenuUtils {

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public static GuiItem getBackItem() {
        return buildGuiItem(
            lang.getString("items.back.material"),
            lang.getString("items.back.name"),
            lang.getStringList("items.back.lore")
        );
    }

    public static GuiItem getCloseItem() {
        return buildGuiItem(
            lang.getString("items.close.material"),
            lang.getString("items.close.name"),
            lang.getStringList("items.close.lore")
        );
    }

    public static GuiItem getPreviousPageItem() {
        return buildGuiItem(
            lang.getString("items.previous-page.material"),
            lang.getString("items.previous-page.name"),
            lang.getStringList("items.previous-page.lore")
        );
    }
    
    public static GuiItem getNextPageItem() {
        return buildGuiItem(
            lang.getString("items.next-page.material"),
            lang.getString("items.next-page.name"),
            lang.getStringList("items.next-page.lore")
        );
    }

    public static GuiItem getFillerItem() {
        return buildGuiItem(
            lang.getString("items.filler.material"),
            lang.getString("items.filler.name"),
            lang.getStringList("items.filler.lore")
        );
    }

    public static GuiItem getConfirmItem() {
        return buildGuiItem(
            lang.getString("items.confirm.material"),
            lang.getString("items.confirm.name"),
            lang.getStringList("items.confirm.lore")
        );
    }

    public static GuiItem getCancelItem() {
        return buildGuiItem(
            lang.getString("items.cancel.material"),
            lang.getString("items.cancel.name"),
            lang.getStringList("items.cancel.lore")
        );
    }

    public static GuiItem getConfigItem(String name, String desc, Boolean value, Material trueMaterial, Material falseMaterial) {
        String displayName = lang.getString("items.config.name").replace("%name%", name);
        List<String> displayDesc = lang.getStringList("items.config.lore");
        List<String> processedDesc = new ArrayList<>();
        for (String line : displayDesc) {
            if (line.contains("%desc%")) line = line.replace("%desc%", desc);
            if (line.contains("%value%")) line = line.replace("%value%", value ? lang.getString("items.config.value-true") : lang.getString("items.config.value-false"));
            processedDesc.add(line);
        }

        return buildGuiItem(value ? trueMaterial : falseMaterial, displayName, processedDesc);
    }

    private static GuiItem buildGuiItem(String materialName, String name, List<String> lore) {
        ItemBuilder builder = ItemBuilder.from(getMaterialFromString(materialName))
            .name(MiniMessage.miniMessage().deserialize("<!italic>" + name));
            
        List<Component> components = new ArrayList<>();
        if (lore != null && !lore.isEmpty()) {
            for (String line : lore) {
                components.add(MiniMessage.miniMessage().deserialize("<!italic>" + line));
            }
            builder.lore(components);
        }

        return builder.asGuiItem();
    }

    private static GuiItem buildGuiItem(Material material, String name, List<String> lore) {
        ItemBuilder builder = ItemBuilder.from(material)
            .name(MiniMessage.miniMessage().deserialize("<!italic>" + name));
        
        List<Component> components = new ArrayList<>();
        if (lore != null && !lore.isEmpty()) {
            for (String line : lore) {
                components.add(MiniMessage.miniMessage().deserialize("<!italic>" + line));
            }
            builder.lore(components);
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
