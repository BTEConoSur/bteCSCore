package com.bteconosur.core.menu.config;

import java.util.HashSet;
import java.util.Set;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.ConfigurationService;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.util.ConfigurationKey;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReviewerConfigMenu extends Menu {

    private Configuration configuration;
    private Player btecsPlayer;
    private Set<ConfigurationKey> selectedKeys = new HashSet<>();
    
    public ReviewerConfigMenu(Player player) {
        super("Configuración de Reviewer", 3, player);
        this.configuration = player.getConfiguration();
        this.btecsPlayer = player;
    }

    public ReviewerConfigMenu(Player player, Menu previousMenu) {
        super("Configuración de Reviewer", 3, player, previousMenu);
        this.configuration = player.getConfiguration();
        this.btecsPlayer = player;
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        gui.getFiller().fill(MenuUtils.getFillerItem());

        Language language = btecsPlayer.getConfiguration().getLang();
        Boolean dsNotifications = configuration.getReviewerDsNotifications();   
        gui.setItem(2, 2, MenuUtils.getReviewerConfigItem(language, "ds-notifications", dsNotifications));
        gui.addSlotAction(2, 2, event -> {
            addSelectedKey(ConfigurationKey.REVIEWER_DS_NOTIFICATIONS);
            gui.updateItem(2, 2, MenuUtils.getReviewerConfigItem(language, "ds-notifications", selectedKeys.contains(ConfigurationKey.REVIEWER_DS_NOTIFICATIONS) ? !dsNotifications : dsNotifications));
        });

        gui.addSlotAction(rows, 5, event -> {
            if (selectedKeys.isEmpty()) return;
            event.getWhoClicked().closeInventory();
            ConfigurationService.save(btecsPlayer, selectedKeys);
            PlayerLogger.info(btecsPlayer, LanguageHandler.getText(language, "config-updated"), (String) null);
        });
        
        return gui;
    }

    private void addSelectedKey(ConfigurationKey key) {
        if (selectedKeys.contains(key)) selectedKeys.remove(key);
        else selectedKeys.add(key);
        if (selectedKeys.isEmpty()) gui.updateItem(rows, 5, MenuUtils.getFillerItem());
        else gui.updateItem(rows, 5, MenuUtils.getSaveItem(btecsPlayer.getConfiguration().getLang()));
    }
}
