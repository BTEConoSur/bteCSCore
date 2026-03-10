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

public class ScoreboardConfigMenu extends Menu {

    private Configuration configuration;
    private Player btecsPlayer;
    private Set<ConfigurationKey> selectedKeys = new HashSet<>();
    
    public ScoreboardConfigMenu(Player player) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.scoreboard-config"), 3, player);
        this.configuration = player.getConfiguration();
        this.btecsPlayer = player;
    }

    public ScoreboardConfigMenu(Player player, Menu previousMenu) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.scoreboard-config"), 3, player, previousMenu);
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
        Boolean scoreboardOnline = configuration.getScoreboardOnline();   
        gui.setItem(2, 3, MenuUtils.getScoreboardConfigItem(language, "online", scoreboardOnline));
        gui.addSlotAction(2, 3, event -> {
            addSelectedKey(ConfigurationKey.SCOREBOARD_ONLINE);
            gui.updateItem(2, 3, MenuUtils.getScoreboardConfigItem(language, "online", selectedKeys.contains(ConfigurationKey.SCOREBOARD_ONLINE) ? !scoreboardOnline : scoreboardOnline));
        });

        Boolean scoreboardPlayer = configuration.getScoreboardPlayer();   
        gui.setItem(2, 5, MenuUtils.getScoreboardConfigItem(language, "player", scoreboardPlayer));
        gui.addSlotAction(2, 5, event -> {
            addSelectedKey(ConfigurationKey.SCOREBOARD_PLAYER);
            gui.updateItem(2, 5, MenuUtils.getScoreboardConfigItem(language, "player", selectedKeys.contains(ConfigurationKey.SCOREBOARD_PLAYER) ? !scoreboardPlayer : scoreboardPlayer));
        });

        Boolean scoreboardProyecto = configuration.getScoreboardProyecto();   
        gui.setItem(2, 7, MenuUtils.getScoreboardConfigItem(language, "proyecto", scoreboardProyecto));
        gui.addSlotAction(2, 7, event -> {
            addSelectedKey(ConfigurationKey.SCOREBOARD_PROYECTO);
            gui.updateItem(2, 7, MenuUtils.getScoreboardConfigItem(language, "proyecto", selectedKeys.contains(ConfigurationKey.SCOREBOARD_PROYECTO) ? !scoreboardProyecto : scoreboardProyecto));
        });

        gui.addSlotAction(rows, 5, event -> {
            if (selectedKeys.isEmpty()) return;
            event.getWhoClicked().closeInventory();
            boolean resultOnline  = selectedKeys.contains(ConfigurationKey.SCOREBOARD_ONLINE) ? !scoreboardOnline  : scoreboardOnline;
            boolean resultPlayer  = selectedKeys.contains(ConfigurationKey.SCOREBOARD_PLAYER) ? !scoreboardPlayer  : scoreboardPlayer;
            boolean resultProyecto = selectedKeys.contains(ConfigurationKey.SCOREBOARD_PROYECTO) ? !scoreboardProyecto : scoreboardProyecto;
            boolean anyEnabled = resultOnline || resultPlayer || resultProyecto;
            if (!anyEnabled && configuration.getGeneralScoreboard()) selectedKeys.add(ConfigurationKey.GENERAL_SCOREBOARD);
            else if (anyEnabled && !configuration.getGeneralScoreboard()) selectedKeys.add(ConfigurationKey.GENERAL_SCOREBOARD);
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
