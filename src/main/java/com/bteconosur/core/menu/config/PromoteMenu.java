package com.bteconosur.core.menu.config;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PromoteMenu extends Menu {

    private Player BTECSPlayer;
    private Pais previousPais;

    public PromoteMenu(Player player, String title) {
        super(title, 4, player);
        this.BTECSPlayer = player;
    }

    public PromoteMenu(Player player, Menu previousMenu, String title) {
        super(title, 4, player, previousMenu);
        this.BTECSPlayer = player;
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        YamlConfiguration lang = ConfigHandler.getInstance().getLang();
        String messageSwitch = lang.getString("country-prefix-switched");
        String messageSet = lang.getString("country-prefix-set");
        Player playerMenu = Player.getBTECSPlayer(player);

        gui.getFiller().fill(MenuUtils.getFillerItem());

        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Pais arg = PaisRegistry.getInstance().getArgentina();
        previousPais = BTECSPlayer.getPaisPrefix();
        gui.setItem(2,2, MenuUtils.getArgentinaHeadItem(arg.equals(previousPais)));
        gui.addSlotAction(2,2, event -> {
            if (arg.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(arg);
            playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%paisPrefix%", lang.getString("mc-prefixes.pais.argentina")), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%paisPrefix%", lang.getString("mc-prefixes.pais.argentina")).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            previousPais = arg;
            updateItems();
        });
        
        return gui;
    }

    private void updateItems() {
    }

    public void setBTECSPlayer(Player BTECSPlayer) {
        this.BTECSPlayer = BTECSPlayer;
    }

}
