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

public class GeneralConfigMenu extends Menu {

    private final Configuration configuration;
    
    public GeneralConfigMenu(Player player) {
        super("Configuración General", 5, player);
        this.configuration = player.getConfiguration();
    }

    public GeneralConfigMenu(Player player, Menu previousMenu) {
        super("Configuración General", 5, player, previousMenu);
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

        gui.setItem(2, 2, MenuUtils.getGeneralConfigItem("global-chat-on-join", configuration.getGeneralGlobalChatOnJoin()));
        gui.addSlotAction(2, 2, event -> {
            ConfigurationService.toggle(player.getUniqueId(), ConfigurationKey.GENERAL_GLOBAL_CHAT_ON_JOIN); // TODO: Capaz es mejor obtener la configuracion con configurationService 
            gui.updateItem(2, 2, MenuUtils.getGeneralConfigItem("global-chat-on-join", configuration.getGeneralGlobalChatOnJoin()));
        });
        
        return gui;
    }

}
