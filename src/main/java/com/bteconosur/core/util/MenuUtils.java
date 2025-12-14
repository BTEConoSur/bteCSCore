package com.bteconosur.core.util;

import org.bukkit.Material;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MenuUtils {

    public static GuiItem getBackItem() {
        return ItemBuilder.from(Material.STRUCTURE_VOID)
            .name(MiniMessage.miniMessage().deserialize("Volver")).asGuiItem();
    }

    public static GuiItem getCloseItem() {
        return ItemBuilder.from(Material.BARRIER)
            .name(MiniMessage.miniMessage().deserialize("Cerrar")).asGuiItem();
    }

    public static GuiItem getPreviousPageItem() {
        return ItemBuilder.from(Material.STONE_BUTTON)
            .name(MiniMessage.miniMessage().deserialize("Página Anterior")).asGuiItem();
    }
    
    public static GuiItem getNextPageItem() {
        return ItemBuilder.from(Material.STONE_BUTTON)
            .name(MiniMessage.miniMessage().deserialize("Página Siguiente")).asGuiItem();
    }

    public static GuiItem getFillerItem() {
        return ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(Component.text(" ")).asGuiItem();
    }

    public static GuiItem getConfirmItem() {
        return ItemBuilder.from(Material.GREEN_WOOL)
            .name(MiniMessage.miniMessage().deserialize("<green>Confirmar")).asGuiItem();
    }

    public static GuiItem getCancelItem() {
        return ItemBuilder.from(Material.RED_WOOL)
            .name(MiniMessage.miniMessage().deserialize("<red>Cancelar")).asGuiItem();
    }
}
