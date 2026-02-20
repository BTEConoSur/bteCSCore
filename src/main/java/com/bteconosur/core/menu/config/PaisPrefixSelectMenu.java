package com.bteconosur.core.menu.config;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PaisPrefixSelectMenu extends Menu {

    private Player BTECSPlayer;
    private Pais previousPais;
    private Language language;

    public PaisPrefixSelectMenu(Player player) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.pais-prefix-select"), 3, player);
        this.BTECSPlayer = player;
    }

    public PaisPrefixSelectMenu(Player player, Menu previousMenu) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.pais-prefix-select"), 3, player, previousMenu);
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

        language = Player.getBTECSPlayer(player).getLanguage();
        String messageSwitch = LanguageHandler.getText(language, "country-prefix.switched");
        String messageSet = LanguageHandler.getText(language, "country-prefix.set");
        Player playerMenu = Player.getBTECSPlayer(player);
        
        gui.getFiller().fill(MenuUtils.getFillerItem());
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Pais arg = PaisRegistry.getInstance().getArgentina();
        previousPais = BTECSPlayer.getPaisPrefix();

        gui.setItem(2,2, MenuUtils.getArgentinaHeadItem(arg.equals(previousPais), language));
        gui.addSlotAction(2,2, event -> {
            if (arg.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(arg);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), BTECSPlayer), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(messageSet, language, BTECSPlayer), (String) null);
            previousPais = arg;
            updateItems();
        });

        Pais chile = PaisRegistry.getInstance().getChile();
        gui.setItem(2,3, MenuUtils.getChileHeadItem(chile.equals(previousPais), language));
        gui.addSlotAction(2,3, event -> {
            if (chile.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(chile);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), BTECSPlayer), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(messageSet, language, BTECSPlayer), (String) null);
            previousPais = chile;
            updateItems();
        });

        Pais peru = PaisRegistry.getInstance().getPeru();
        gui.setItem(2,4, MenuUtils.getPeruHeadItem(peru.equals(previousPais), language));
        gui.addSlotAction(2,4, event -> {
            if (peru.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(peru);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), BTECSPlayer), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(messageSet, language, BTECSPlayer), (String) null);
            previousPais = peru;
            updateItems();
        });

        gui.setItem(2,5, MenuUtils.getInternationalHead(previousPais == null, language));
        gui.addSlotAction(2,5, event -> {
            if (previousPais == null) return;
            BTECSPlayer.setPaisPrefix(null);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), BTECSPlayer), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(messageSet, language, BTECSPlayer), (String) null);
            previousPais = null;
            updateItems();
        });

        Pais bolivia = PaisRegistry.getInstance().getBolivia();
        gui.setItem(2,6, MenuUtils.getBoliviaHeadItem(bolivia.equals(previousPais), language));
        gui.addSlotAction(2,6, event -> {
            if (bolivia.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(bolivia);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), BTECSPlayer), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(messageSet, language, BTECSPlayer), (String) null);
            previousPais = bolivia;
            updateItems();
        });

        Pais uruguay = PaisRegistry.getInstance().getUruguay();
        gui.setItem(2,7, MenuUtils.getUruguayHeadItem(uruguay.equals(previousPais), language));
        gui.addSlotAction(2,7, event -> {
            if (uruguay.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(uruguay);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), BTECSPlayer), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(messageSet, language, BTECSPlayer), (String) null);
            previousPais = uruguay;
            updateItems();
        });

        Pais paraguay = PaisRegistry.getInstance().getParaguay();
        gui.setItem(2,8, MenuUtils.getParaguayHeadItem(paraguay.equals(previousPais), language));
        gui.addSlotAction(2,8, event -> {
            if (paraguay.equals(previousPais)) return;
            BTECSPlayer.setPaisPrefix(paraguay);
            BTECSPlayer = playerRegistry.merge(BTECSPlayer.getUuid());
            PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), BTECSPlayer), (String) null);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(messageSet, language, BTECSPlayer), (String) null);
            previousPais = paraguay;
            updateItems();
        });
        
        return gui;
    }

    private void updateItems() {
        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        gui.updateItem(2,2, MenuUtils.getArgentinaHeadItem(paisRegistry.getArgentina().equals(previousPais), language));
        gui.updateItem(2,3, MenuUtils.getChileHeadItem(paisRegistry.getChile().equals(previousPais), language));
        gui.updateItem(2,4, MenuUtils.getPeruHeadItem(paisRegistry.getPeru().equals(previousPais), language));
        gui.updateItem(2,5, MenuUtils.getInternationalHead(previousPais == null, language));
        gui.updateItem(2,6, MenuUtils.getBoliviaHeadItem(paisRegistry.getBolivia().equals(previousPais), language));
        gui.updateItem(2,7, MenuUtils.getUruguayHeadItem(paisRegistry.getUruguay().equals(previousPais), language));
        gui.updateItem(2,8, MenuUtils.getParaguayHeadItem(paisRegistry.getParaguay().equals(previousPais), language));
    }

    public void setBTECSPlayer(Player BTECSPlayer) {
        this.BTECSPlayer = BTECSPlayer;
    }

}
