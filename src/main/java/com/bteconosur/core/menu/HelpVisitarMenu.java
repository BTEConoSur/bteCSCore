package com.bteconosur.core.menu;

import org.bukkit.entity.Player;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.MenuUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class HelpVisitarMenu extends Menu {

    public HelpVisitarMenu(Player player, String title) {
        super(title, 3, player);
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        gui.setItem(2, 4, MenuUtils.getHelpVisitarAnyplaceItem(language));
        gui.addSlotAction(2, 4, event -> {
            HelpVisitarAnyplaceMenu anyplaceMenu = new HelpVisitarAnyplaceMenu(player, LanguageHandler.getText(language, "gui-titles.visit-anyplace") , this);
            anyplaceMenu.open();
        });

        gui.setItem(2, 6, MenuUtils.getHelpVisitarBuiltplaceItem(language));
        gui.addSlotAction(2, 6, event -> {
            HelpVisitarBuiltplaceMenu builtplaceMenu = new HelpVisitarBuiltplaceMenu(player, LanguageHandler.getText(language, "gui-titles.visit-builtplace"), this);
            builtplaceMenu.open();
        });

        gui.getFiller().fill(MenuUtils.getFillerItem());
        return gui;
    }

}
