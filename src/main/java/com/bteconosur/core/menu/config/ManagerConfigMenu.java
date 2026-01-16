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

public class ManagerConfigMenu extends Menu {

    private final Configuration configuration;
    
    public ManagerConfigMenu(Player player) {
        super("Configuración de Manager", 5, player);
        this.configuration = player.getConfiguration();
    }

    public ManagerConfigMenu(Player player, Menu previousMenu) {
        super("Configuración de Manager", 5, player, previousMenu);
        this.configuration = player.getConfiguration();
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        gui.getFiller().fill(MenuUtils.getFillerItem());

        gui.setItem(2, 2, MenuUtils.getManagerConfigItem("notifications", configuration.getManagerDsNotifications()));
        gui.addSlotAction(2, 2, event -> {
            ConfigurationService.toggle(player.getUniqueId(), ConfigurationKey.MANAGER_DS_NOTIFICATIONS); // TODO: Capaz es mejor obtener la configuracion con configurationService 
            gui.updateItem(2, 2, MenuUtils.getManagerConfigItem("notifications", configuration.getManagerDsNotifications()));
        });
        
        return gui;
    }

}
