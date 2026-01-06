package com.bteconosur.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

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

    public static GuiItem getNotePadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.notepad.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected-chat"));
        
        return buildGuiItem(
            lang.getString("items.notepad.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.notepad.name"),
            lore
        );
    }   

    public static GuiItem getGeneralConfigItem(String name, Boolean value) {
        return getConfigItem("general", name, value);
    }

    public static GuiItem getManagerConfigItem(String name, Boolean value) {
        return getConfigItem("manager", name, value);
    }

    public static GuiItem getReviewerConfigItem(String name, Boolean value) {
        return getConfigItem("reviewer", name, value);
    }

    private static GuiItem getConfigItem(String context, String name, Boolean value) {
        String displayName = lang.getString("items.config.name");
        displayName = displayName.replace("%name%", lang.getString("items.config.configs." + context + "." + name + ".name"));

        List<String> displayDesc = lang.getStringList("items.config.configs." + context + "." + name + ".desc");
        List<String> processedDesc = new ArrayList<>();
        for (String line : displayDesc) {
            String descLine = lang.getString("items.config.desc-line");
            descLine = descLine.replace("%desc%", line);
            processedDesc.add(descLine);
        }

        String valueLine = lang.getString("items.config.value-line");
        valueLine = valueLine.replace("%value%", value ? lang.getString("items.config.value-true") : lang.getString("items.config.value-false"));
        processedDesc.add(valueLine);

        String trueMaterial = lang.getString("items.config.configs." + context + "." + name + ".material-true");
        String falseMaterial = lang.getString("items.config.configs." + context + "." + name + ".material-false");
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

    private static GuiItem buildGuiItem(ItemStack itemStack, String name, List<String> lore) {
        ItemBuilder builder = ItemBuilder.from(itemStack)
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

    public static GuiItem getArgentinaHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.argentina-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected-chat"));
        return buildGuiItem(
            buildHead(lang.getString("items.argentina-head.base64")),
            (isSelected ? "<b>" : "") + lang.getString("items.argentina-head.name"),
            lore
        );
    }

    public static GuiItem getChileHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.chile-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected-chat"));
        return buildGuiItem(
            buildHead(lang.getString("items.chile-head.base64")),
            (isSelected ? "<b>" : "") + lang.getString("items.chile-head.name"),
            lore
        );
    }

    public static GuiItem getUruguayHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.uruguay-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected-chat"));
        return buildGuiItem(
            buildHead(lang.getString("items.uruguay-head.base64")),
            (isSelected ? "<b>" : "") + lang.getString("items.uruguay-head.name"),
            lore
        );
    }

    public static GuiItem getParaguayHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.paraguay-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected-chat"));
        return buildGuiItem(
            buildHead(lang.getString("items.paraguay-head.base64")),
            (isSelected ? "<b>" : "") + lang.getString("items.paraguay-head.name"),
            lore
        );
    }

    public static GuiItem getBoliviaHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.bolivia-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected-chat"));
        return buildGuiItem(
            buildHead(lang.getString("items.bolivia-head.base64")),
            (isSelected ? "<b>" : "") + lang.getString("items.bolivia-head.name"),
            lore
        );
    }

    public static GuiItem getPeruHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.peru-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected-chat"));   
        return buildGuiItem(
            buildHead(lang.getString("items.peru-head.base64")),
            (isSelected ? "<b>" : "") + lang.getString("items.peru-head.name"),
            lore
        );
    }

    public static GuiItem getGlobalChatHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.global-chat-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected-chat"));
        return buildGuiItem(
            buildHead(lang.getString("items.global-chat-head.base64")),
            (isSelected ? "<b>" : "") + lang.getString("items.global-chat-head.name"),
            lore
        );
    }

    public static GuiItem getInternationalHead(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.international-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected-chat"));
        return buildGuiItem(
            buildHead(lang.getString("items.international-head.base64")),
            (isSelected ? "<b>" : "") + lang.getString("items.international-head.name"),
            lore
        );
    }

    private static ItemStack buildHead(String base64) {
        //TODO: Ver de usar el plugin de cabezas
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures", base64));
        headMeta.setPlayerProfile(profile);
        head.setItemMeta(headMeta);
        return head;
    }
}
