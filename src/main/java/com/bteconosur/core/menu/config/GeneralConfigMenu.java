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

public class GeneralConfigMenu extends Menu {

    private Configuration configuration;
    private Player btecsPlayer;
    private Set<ConfigurationKey> selectedKeys = new HashSet<>();
    
    public GeneralConfigMenu(Player player) {
        super("Configuración General", 5, player);
        this.configuration = player.getConfiguration();
        this.btecsPlayer = player;
    }

    public GeneralConfigMenu(Player player, Menu previousMenu) {
        super("Configuración General", 5, player, previousMenu);
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
        Boolean globalChatOnJoin = configuration.getGeneralGlobalChatOnJoin();
        gui.setItem(2, 2, MenuUtils.getGeneralConfigItem(language, "global-chat-on-join", globalChatOnJoin));
        gui.addSlotAction(2, 2, event -> {
            addSelectedKey(ConfigurationKey.GENERAL_GLOBAL_CHAT_ON_JOIN);
            gui.updateItem(2, 2, MenuUtils.getGeneralConfigItem(language, "global-chat-on-join", selectedKeys.contains(ConfigurationKey.GENERAL_GLOBAL_CHAT_ON_JOIN) ? !globalChatOnJoin : globalChatOnJoin));
        });

        Boolean simultaneousNotifications = configuration.getGeneralSimultaneousNotifications();
        gui.setItem(2, 4, MenuUtils.getGeneralConfigItem(language, "simultaneous-notifications", simultaneousNotifications));
        gui.addSlotAction(2, 4, event -> {
            addSelectedKey(ConfigurationKey.GENERAL_SIMULTANEOUS_NOTIFICATIONS);
            gui.updateItem(2, 4, MenuUtils.getGeneralConfigItem(language, "simultaneous-notifications", selectedKeys.contains(ConfigurationKey.GENERAL_SIMULTANEOUS_NOTIFICATIONS) ? !simultaneousNotifications : simultaneousNotifications));
        });

        Boolean paisBorder = configuration.getGeneralPaisBorder();
        gui.setItem(2, 6, MenuUtils.getGeneralConfigItem(language, "pais-border", paisBorder));
        gui.addSlotAction(2, 6, event -> {
            addSelectedKey(ConfigurationKey.GENERAL_PAIS_BORDER);
            gui.updateItem(2, 6, MenuUtils.getGeneralConfigItem(language, "pais-border", selectedKeys.contains(ConfigurationKey.GENERAL_PAIS_BORDER) ? !paisBorder : paisBorder));
        });

        Boolean labelBorder = configuration.getGeneralLabelBorder();
        gui.setItem(2, 8, MenuUtils.getGeneralConfigItem(language, "label-border", labelBorder));
        gui.addSlotAction(2, 8, event -> {
            addSelectedKey(ConfigurationKey.GENERAL_LABEL_BORDER);
            gui.updateItem(2, 8, MenuUtils.getGeneralConfigItem(language, "label-border", selectedKeys.contains(ConfigurationKey.GENERAL_LABEL_BORDER) ? !labelBorder : labelBorder));
        });

        Boolean projectTitle = configuration.getGeneralProjectTitle();
        gui.setItem(3, 3, MenuUtils.getGeneralConfigItem(language, "project-title", projectTitle));
        gui.addSlotAction(3, 3, event -> {
            addSelectedKey(ConfigurationKey.GENERAL_PROJECT_TITLE);
            gui.updateItem(3, 3, MenuUtils.getGeneralConfigItem(language, "project-title", selectedKeys.contains(ConfigurationKey.GENERAL_PROJECT_TITLE) ? !projectTitle : projectTitle));
        });

        Boolean divisionTitle = configuration.getGeneralDivisionTitle();
        gui.setItem(3, 5, MenuUtils.getGeneralConfigItem(language, "division-title", divisionTitle));
        gui.addSlotAction(3, 5, event -> {
            addSelectedKey(ConfigurationKey.GENERAL_DIVISION_TITLE);
            gui.updateItem(3, 5, MenuUtils.getGeneralConfigItem(language, "division-title", selectedKeys.contains(ConfigurationKey.GENERAL_DIVISION_TITLE) ? !divisionTitle : divisionTitle));
        });

        Boolean scoreboard = configuration.getGeneralScoreboard();
        gui.setItem(3, 7, MenuUtils.getGeneralConfigItem(language, "scoreboard", scoreboard));
        gui.addSlotAction(3, 7, event -> {
            addSelectedKey(ConfigurationKey.GENERAL_SCOREBOARD);
            gui.updateItem(3, 7, MenuUtils.getGeneralConfigItem(language, "scoreboard", selectedKeys.contains(ConfigurationKey.GENERAL_SCOREBOARD) ? !scoreboard : scoreboard));
        });

        for (int i = 1; i <= 9; i++) {
            gui.setItem(4, i, MenuUtils.getSeparatorItem());
        }
        

        gui.setItem(5, 4, MenuUtils.getPaisPrefixConfigItem(language));
        gui.addSlotAction(5, 4, event -> {
            new PaisPrefixSelectMenu(btecsPlayer, LanguageHandler.getText(language, "gui-titles.pais-prefix-select")).open();
        });

        gui.setItem(5, 6, MenuUtils.getLangConfigItem(language));
        gui.addSlotAction(5, 6, event -> {
            new LanguageSelectMenu(btecsPlayer, LanguageHandler.getText(language, "gui-titles.language-select")).open();
        });
        
        gui.addSlotAction(4, 5, event -> {
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
        if (selectedKeys.isEmpty()) gui.updateItem(4, 5, MenuUtils.getSeparatorItem());
        else gui.updateItem(4, 5, MenuUtils.getSaveItem(btecsPlayer.getConfiguration().getLang()));
    }

}
