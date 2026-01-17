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

public class PaisPrefixSelectMenu extends Menu {

    private Player BTECSPlayer;
    private Pais previousPais;

    public PaisPrefixSelectMenu(Player player) {
        super(ConfigHandler.getInstance().getLang().getString("gui-titles.pais-prefix-select"), 3, player);
        this.BTECSPlayer = player;
    }

    public PaisPrefixSelectMenu(Player player, Menu previousMenu) {
        super(ConfigHandler.getInstance().getLang().getString("gui-titles.pais-prefix-select"), 3, player, previousMenu);
        this.BTECSPlayer = player;
    }

    public PaisPrefixSelectMenu(Player player, String title) {
        super(title, 4, player);
        this.BTECSPlayer = player;
    }

    public PaisPrefixSelectMenu(Player player, Menu previousMenu, String title) {
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
            PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%country%", arg.getNombrePublico()), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%country%", arg.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            previousPais = arg;
            updateItems();
        });

        Pais chile = PaisRegistry.getInstance().getChile();
        gui.setItem(2,3, MenuUtils.getChileHeadItem(chile.equals(previousPais)));
        gui.addSlotAction(2,3, event -> {
            if (chile.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(chile);
            playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%country%", chile.getNombrePublico()), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%country%", chile.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            previousPais = chile;
            updateItems();
        });

        Pais peru = PaisRegistry.getInstance().getPeru();
        gui.setItem(2,4, MenuUtils.getPeruHeadItem(peru.equals(previousPais)));
        gui.addSlotAction(2,4, event -> {
            if (peru.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(peru);
            playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%country%", peru.getNombrePublico()), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%country%", peru.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            previousPais = peru;
            updateItems();
        });

        gui.setItem(2,5, MenuUtils.getInternationalHead(previousPais == null));
        gui.addSlotAction(2,5, event -> {
            if (previousPais == null) return;
            BTECSPlayer.setPaisPrefix(null);
            playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%country%", "Internacional"), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%country%", "Internacional").replace("%player%", BTECSPlayer.getNombre()), (String) null);
            previousPais = null;
            updateItems();
        });

        Pais bolivia = PaisRegistry.getInstance().getBolivia();
        gui.setItem(2,6, MenuUtils.getBoliviaHeadItem(bolivia.equals(previousPais)));
        gui.addSlotAction(2,6, event -> {
            if (bolivia.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(bolivia);
            playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%country%", bolivia.getNombrePublico()), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%country%", bolivia.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            previousPais = bolivia;
            updateItems();
        });

        Pais uruguay = PaisRegistry.getInstance().getUruguay();
        gui.setItem(2,7, MenuUtils.getUruguayHeadItem(uruguay.equals(previousPais)));
        gui.addSlotAction(2,7, event -> {
            if (uruguay.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(uruguay);
            playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%country%", uruguay.getNombrePublico()), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%country%", uruguay.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            previousPais = uruguay;
            updateItems();
        });

        Pais paraguay = PaisRegistry.getInstance().getParaguay();
        gui.setItem(2,8, MenuUtils.getParaguayHeadItem(paraguay.equals(previousPais)));
        gui.addSlotAction(2,8, event -> {
            if (paraguay.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(paraguay);
            playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%country%", paraguay.getNombrePublico()), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%country%", paraguay.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            previousPais = paraguay;
            updateItems();
        });
        
        return gui;
    }

    private void updateItems() {
        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        gui.updateItem(2,2, MenuUtils.getArgentinaHeadItem(paisRegistry.getArgentina().equals(previousPais)));
        gui.updateItem(2,3, MenuUtils.getChileHeadItem(paisRegistry.getChile().equals(previousPais)));
        gui.updateItem(2,4, MenuUtils.getPeruHeadItem(paisRegistry.getPeru().equals(previousPais)));
        gui.updateItem(2,5, MenuUtils.getInternationalHead(previousPais == null));
        gui.updateItem(2,6, MenuUtils.getBoliviaHeadItem(paisRegistry.getBolivia().equals(previousPais)));
        gui.updateItem(2,7, MenuUtils.getUruguayHeadItem(paisRegistry.getUruguay().equals(previousPais)));
        gui.updateItem(2,8, MenuUtils.getParaguayHeadItem(paisRegistry.getParaguay().equals(previousPais)));
    }

    public void setBTECSPlayer(Player BTECSPlayer) {
        this.BTECSPlayer = BTECSPlayer;
    }

}
