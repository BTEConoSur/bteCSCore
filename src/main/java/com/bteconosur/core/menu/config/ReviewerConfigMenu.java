package com.bteconosur.core.menu.config;

import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.ConfigurationService;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.util.ConfigurationKey;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReviewerConfigMenu extends Menu {

    private Configuration configuration;
    private Player btecsPlayer;
    
    public ReviewerConfigMenu(Player player) {
        super("Configuración de Reviewer", 5, player);
        this.configuration = player.getConfiguration();
        this.btecsPlayer = player;
    }

    public ReviewerConfigMenu(Player player, Menu previousMenu) {
        super("Configuración de Reviewer", 5, player, previousMenu);
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

        gui.setItem(2, 2, MenuUtils.getReviewerConfigItem("ds-notifications", configuration.getReviewerDsNotifications()));
        gui.addSlotAction(2, 2, event -> {
            btecsPlayer = ConfigurationService.toggle(btecsPlayer, ConfigurationKey.REVIEWER_DS_NOTIFICATIONS); // TODO: Capaz es mejor obtener la configuracion con configurationService 
            configuration = btecsPlayer.getConfiguration();
            gui.updateItem(2, 2, MenuUtils.getReviewerConfigItem("ds-notifications", configuration.getReviewerDsNotifications()));
        });
        
        return gui;
    }
}
