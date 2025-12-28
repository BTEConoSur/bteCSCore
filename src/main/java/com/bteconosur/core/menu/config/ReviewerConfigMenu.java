package com.bteconosur.core.menu.config;

import org.bukkit.Material;

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

    private final Configuration configuration;
    
    public ReviewerConfigMenu(Player player) {
        super("Configuración de Reviewer", 5, player);
        this.configuration = player.getConfiguration();
    }

    public ReviewerConfigMenu(Player player, Menu previousMenu) {
        super("Configuración de Reviewer", 5, player, previousMenu);
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

        gui.setItem(2, 2, MenuUtils.getConfigItem("Toggle Test", "Toggle para testear.", configuration.getReviewerToggleTest(), Material.RED_GLAZED_TERRACOTTA, Material.RED_CONCRETE));
        gui.addSlotAction(2, 2, event -> {
            ConfigurationService.toggle(player.getUniqueId(), ConfigurationKey.REVIEWER_TOGGLE_TEST); // TODO: Capaz es mejor obtener la configuracion con configurationService 
            gui.updateItem(2, 2, MenuUtils.getConfigItem("Toggle Test", "Toggle para testear.", configuration.getReviewerToggleTest(), Material.RED_GLAZED_TERRACOTTA, Material.RED_CONCRETE));
        });
        
        return gui;
    }
}
