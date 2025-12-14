package com.bteconosur.core.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.bteconosur.core.util.MenuUtils;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TestSimpleMenu extends Menu {

    private ConfirmationMenu confirmationMenu;

    public TestSimpleMenu(Player player) {
        super("Menú Simple de Prueba", 3, player);
    }

    public TestSimpleMenu(Player player, Menu previousMenu) {
        super("Menú Simple de Prueba", 3, player, previousMenu);
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        gui.setItem(1, 3, ItemBuilder.from(Material.DIAMOND)
            .name(Component.text("Diamante"))
            .lore(Component.text("Click para recibir un mensaje"))
            .asGuiItem(event -> {
                player.sendMessage(Component.text("clickeaste en un diamante!"));
            }));

            gui.setItem(1, 5, ItemBuilder.from(Material.GOLD_INGOT)
                .name(Component.text("Abrir menú paginado").color(NamedTextColor.GOLD))
                .lore(Component.text("Click para abrir el paginado de prueba"))
                .asGuiItem(event -> {
                    new TestPaginatedMenu(player, this).open();
                }));

        gui.setItem(1, 7, ItemBuilder.from(Material.EMERALD)
            .name(Component.text("Esmeralda").color(NamedTextColor.GREEN))
            .lore(Component.text("Click para cerrar el menú"))
            .asGuiItem(event -> {
                player.closeInventory();
            }));

        gui.setItem(2, 3, ItemBuilder.from(Material.PAPER)
            .name(Component.text("Información").color(NamedTextColor.WHITE))
            .lore(
                Component.text("Este es un menú simple de prueba"),
                Component.text("Creado con el sistema Menu")
            )
            .asGuiItem());

        gui.setItem(2, 7, ItemBuilder.from(Material.BOOK)
            .name(Component.text("Confirmar").color(NamedTextColor.WHITE))
            .lore(
                Component.text("Este es un menú simple de prueba"),
                Component.text("Creado con el sistema Menu")
            )
            .asGuiItem(event -> {
                    confirmationMenu = new ConfirmationMenu("Confirmar????", player, this, confirmClick -> {
                        player.sendMessage(Component.text("¡Has confirmado!").color(NamedTextColor.GREEN));
                        this.open();
                    }, (cancelClick -> {
                        player.sendMessage(Component.text("Has cancelado.").color(NamedTextColor.RED));
                        confirmationMenu.getGui().close(player);
                    }));
                    confirmationMenu.open();  
            }));

        gui.setItem(2, 5, ItemBuilder.from(Material.BOOK)
            .name(Component.text("Confirmar 2").color(NamedTextColor.WHITE))
            .lore(
                Component.text("Este es un menú simple de prueba"),
                Component.text("Creado con el sistema Menu")
            )
            .asGuiItem(event -> {
                    confirmationMenu = new ConfirmationMenu("Confirmar 2????", player, this, confirmClick -> {
                        player.sendMessage(Component.text("¡Has confirmado!").color(NamedTextColor.GREEN));
                        this.open();
                    });
                    confirmationMenu.open();  
            }));
            
        gui.getFiller().fill(MenuUtils.getFillerItem());

        return gui;
    }
}
