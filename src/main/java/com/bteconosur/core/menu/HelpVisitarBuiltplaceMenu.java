package com.bteconosur.core.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class HelpVisitarBuiltplaceMenu extends Menu{

    public HelpVisitarBuiltplaceMenu(Player player, String title, Menu previousMenu) {
        super(title, 3, player, previousMenu);
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        String pluginPrefix = LanguageHandler.getText(language, "plugin-prefix");
        gui.setItem(2, 4, MenuUtils.getHelpVisitarTourItem(language));
        gui.addSlotAction(2, 4, click -> {
            click.getWhoClicked().closeInventory();
            List<String> addressLore = LanguageHandler.getTextList(language, "help-visitar.tour");
            List<String> processedLore = new ArrayList<>();
            for (String line : addressLore) {
                processedLore.add(line.replace("%plugin-prefix%", pluginPrefix));
            }
            String message = String.join("\n", processedLore);
            PlayerLogger.send(player, message, (String) null);
        });

        gui.setItem(2, 6, MenuUtils.getHelpVisitarWarpsItem(language));
        gui.addSlotAction(2, 6, click -> {
            click.getWhoClicked().closeInventory();
            List<String> addressLore = LanguageHandler.getTextList(language, "help-visitar.warps");
            List<String> processedLore = new ArrayList<>();
            for (String line : addressLore) {
                processedLore.add(line.replace("%plugin-prefix%", pluginPrefix));
            }
            String message = String.join("\n", processedLore);
            PlayerLogger.send(player, message, (String) null);
        });

        gui.getFiller().fill(MenuUtils.getFillerItem());
        return gui;
    }

}
