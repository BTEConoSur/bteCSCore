package com.bteconosur.core.menu.chat;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ChatSelectMenu extends Menu {

    private Player BTECSPlayer;

    public ChatSelectMenu(Player player) {
        super(ConfigHandler.getInstance().getLang().getString("gui-titles.chat-select"), 4, player);
        this.BTECSPlayer = player;
    }

    public ChatSelectMenu(Player player, Menu previousMenu) {
        super(ConfigHandler.getInstance().getLang().getString("gui-titles.chat-select"), 4, player, previousMenu);
        this.BTECSPlayer = player;
    }

    public ChatSelectMenu(Player player, String title) {
        super(title, 4, player);
        this.BTECSPlayer = player;
    }

    public ChatSelectMenu(Player player, Menu previousMenu, String title) {
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

        gui.getFiller().fill(MenuUtils.getFillerItem());

        Pais arg = PaisRegistry.getInstance().getArgentina();
        gui.setItem(3,2, MenuUtils.getArgentinaHeadItem(ChatService.isInCountryChat(BTECSPlayer, arg)));
        gui.addSlotAction(3,2, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, arg);
            gui.updateItem(3, 2, MenuUtils.getArgentinaHeadItem(ChatService.isInCountryChat(BTECSPlayer, arg)));
        });

        Pais chile = PaisRegistry.getInstance().getChile();
        gui.setItem(3,3, MenuUtils.getChileHeadItem(ChatService.isInCountryChat(BTECSPlayer, chile)));
        gui.addSlotAction(3,3, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, chile);
            gui.updateItem(3, 3, MenuUtils.getChileHeadItem(ChatService.isInCountryChat(BTECSPlayer, chile)));
        });

        Pais peru = PaisRegistry.getInstance().getPeru();
        gui.setItem(3,4, MenuUtils.getPeruHeadItem(ChatService.isInCountryChat(BTECSPlayer, peru)));
        gui.addSlotAction(3,4, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, peru);
            gui.updateItem(3, 4, MenuUtils.getPeruHeadItem(ChatService.isInCountryChat(BTECSPlayer, peru)));
        });

        gui.setItem(2,4, MenuUtils.getGlobalChatHeadItem(ChatService.isInGlobalChat(BTECSPlayer)));
        gui.addSlotAction(2,4, event -> {
            ChatService.switchChatToGlobal(BTECSPlayer);
            gui.updateItem(2, 4, MenuUtils.getGlobalChatHeadItem(ChatService.isInGlobalChat(BTECSPlayer)));
        });

        gui.setItem(2,6, MenuUtils.getBlockOfNoteItem(false));

        Pais bolivia = PaisRegistry.getInstance().getBolivia();
        gui.setItem(3,6, MenuUtils.getBoliviaHeadItem(ChatService.isInCountryChat(BTECSPlayer, bolivia)));
        gui.addSlotAction(3,6, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, bolivia);
            gui.updateItem(3, 6, MenuUtils.getBoliviaHeadItem(ChatService.isInCountryChat(BTECSPlayer, bolivia)));
        });

        Pais uruguay = PaisRegistry.getInstance().getUruguay();
        gui.setItem(3,7, MenuUtils.getUruguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, uruguay)));
        gui.addSlotAction(3,7, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, uruguay);
            gui.updateItem(3, 7, MenuUtils.getUruguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, uruguay)));
        });

        Pais paraguay = PaisRegistry.getInstance().getParaguay();
        gui.setItem(3,8, MenuUtils.getParaguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, paraguay)));
        gui.addSlotAction(3,8, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, paraguay);
            gui.updateItem(3, 8, MenuUtils.getParaguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, paraguay)));
        });
        
        return gui;
    }

    public void setBTECSPlayer(Player BTECSPlayer) {
        this.BTECSPlayer = BTECSPlayer;
    }

}
