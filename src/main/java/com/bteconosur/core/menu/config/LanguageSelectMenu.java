package com.bteconosur.core.menu.config;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.ConfigurationService;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class LanguageSelectMenu extends Menu {

    private Player BTECSPlayer;

    public LanguageSelectMenu(Player player, String title) {
        super(title, 3, player);
        this.BTECSPlayer = player;
    }

    public LanguageSelectMenu(Player player, Menu previousMenu, String title) {
        super(title, 3, player, previousMenu);
        this.BTECSPlayer = player;
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        Language language = BTECSPlayer.getConfiguration().getLang();
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();   

        gui.getFiller().fill(MenuUtils.getFillerItem());

        gui.setItem(2,2, MenuUtils.getLanguageSelectionItem(Language.SPANISH, language.equals(Language.SPANISH)));
        gui.addSlotAction(2,2, event -> {
            event.getWhoClicked().closeInventory();
            ConfigurationService.setLang(BTECSPlayer, Language.SPANISH);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, LanguageHandler.getText(Language.SPANISH, "language-set").replace("%language%", LanguageHandler.getText(Language.SPANISH, "placeholder.lang-mc.es_ES")), (String) null);
        });

        
        gui.setItem(2,4, MenuUtils.getLanguageSelectionItem(Language.ENGLISH, language.equals(Language.ENGLISH)));
        gui.addSlotAction(2,4, event -> {
            event.getWhoClicked().closeInventory();
            ConfigurationService.setLang(BTECSPlayer, Language.ENGLISH);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, LanguageHandler.getText(Language.ENGLISH, "language-set").replace("%language%", LanguageHandler.getText(Language.ENGLISH, "placeholder.lang-mc.en_US")), (String) null);
        });
        
        return gui;
    }

}
