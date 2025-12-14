package com.bteconosur.core.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TestPaginatedMenu extends PaginatedMenu {

    public TestPaginatedMenu(Player player, Menu previousMenu) {
        super("<gradient:#ff00ff:#00ffff>Menú Paginado de Prueba</gradient>", player, previousMenu);
    }

    public TestPaginatedMenu(Player player) {
        super("<gradient:#ff00ff:#00ffff>Menú Paginado de Prueba</gradient>", player);
    }

    @Override
    protected void populateItems() {
        Material[] materials = {
            Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT,
            Material.EMERALD, Material.COAL, Material.REDSTONE,
            Material.LAPIS_LAZULI, Material.COPPER_INGOT, Material.NETHERITE_INGOT,
            Material.QUARTZ, Material.AMETHYST_SHARD, Material.GLOWSTONE_DUST
        };

        NamedTextColor[] colors = {
            NamedTextColor.AQUA, NamedTextColor.GOLD, NamedTextColor.GRAY,
            NamedTextColor.GREEN, NamedTextColor.DARK_GRAY, NamedTextColor.RED,
            NamedTextColor.BLUE, NamedTextColor.GOLD, NamedTextColor.DARK_RED,
            NamedTextColor.WHITE, NamedTextColor.LIGHT_PURPLE, NamedTextColor.YELLOW
        };

        for (int i = 0; i < 50; i++) {
            Material material = materials[i % materials.length];
            NamedTextColor color = colors[i % colors.length];
            final int itemNumber = i + 1;

            GuiItem item = ItemBuilder.from(material)
                .name(Component.text("Item #" + itemNumber)
                    .color(color))
                .lore(
                    Component.text("Este es el item número " + itemNumber),
                    Component.text("Material: " + material.name()),
                    Component.empty(),
                    Component.text("Click para ver un mensaje")
                )
                .asGuiItem(event -> {
                    player.sendMessage(Component.text("¡Has clickeado el item #" + itemNumber + "!"));
                });

            getPaginatedGui().addItem(item);
        }
    }
}
