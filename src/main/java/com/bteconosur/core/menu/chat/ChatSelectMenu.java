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

    private Player player;

    public ChatSelectMenu(Player player) {
        super(ConfigHandler.getInstance().getLang().getString("gui-titles.chat-select"), 4, player);
        this.player = player;
    }

    public ChatSelectMenu(Player player, Menu previousMenu) {
        super(ConfigHandler.getInstance().getLang().getString("gui-titles.chat-select"), 4, player, previousMenu);
        this.player = player;
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
        gui.setItem(3,2, MenuUtils.getArgentinaHeadItem(ChatService.isInCountryChat(player, arg)));
        gui.addSlotAction(3,2, event -> {
            ChatService.switchChatToCountry(this.player, arg);
            gui.updateItem(3, 2, MenuUtils.getArgentinaHeadItem(ChatService.isInCountryChat(player, arg)));
        });

        Pais chile = PaisRegistry.getInstance().getChile();
        gui.setItem(3,3, MenuUtils.getChileHeadItem(ChatService.isInCountryChat(player, chile)));
        gui.addSlotAction(3,3, event -> {
            ChatService.switchChatToCountry(this.player, chile);
            gui.updateItem(3, 3, MenuUtils.getChileHeadItem(ChatService.isInCountryChat(player, chile)));
        });

        Pais peru = PaisRegistry.getInstance().getPeru();
        gui.setItem(3,4, MenuUtils.getPeruHeadItem(ChatService.isInCountryChat(player, peru)));
        gui.addSlotAction(3,4, event -> {
            ChatService.switchChatToCountry(this.player, peru);
            gui.updateItem(3, 4, MenuUtils.getPeruHeadItem(ChatService.isInCountryChat(player, peru)));
        });

        gui.setItem(2,4, MenuUtils.getGlobalChatHeadItem(ChatService.isInGlobalChat(player)));
        gui.addSlotAction(2,4, event -> {
            ChatService.switchChatToGlobal(this.player);
            gui.updateItem(2, 4, MenuUtils.getGlobalChatHeadItem(ChatService.isInGlobalChat(player)));
        });

        gui.setItem(2,6, MenuUtils.getBlockOfNoteItem(false));

        Pais bolivia = PaisRegistry.getInstance().getBolivia();
        gui.setItem(3,6, MenuUtils.getBoliviaHeadItem(ChatService.isInCountryChat(player, bolivia)));
        gui.addSlotAction(3,6, event -> {
            ChatService.switchChatToCountry(this.player, bolivia);
            gui.updateItem(3, 6, MenuUtils.getBoliviaHeadItem(ChatService.isInCountryChat(player, bolivia)));
        });

        Pais uruguay = PaisRegistry.getInstance().getUruguay();
        gui.setItem(3,7, MenuUtils.getUruguayHeadItem(ChatService.isInCountryChat(player, uruguay)));
        gui.addSlotAction(3,7, event -> {
            ChatService.switchChatToCountry(this.player, uruguay);
            gui.updateItem(3, 7, MenuUtils.getUruguayHeadItem(ChatService.isInCountryChat(player, uruguay)));
        });

        Pais paraguay = PaisRegistry.getInstance().getParaguay();
        gui.setItem(3,8, MenuUtils.getParaguayHeadItem(ChatService.isInCountryChat(player, paraguay)));
        gui.addSlotAction(3,8, event -> {
            ChatService.switchChatToCountry(this.player, paraguay);
            gui.updateItem(3, 8, MenuUtils.getParaguayHeadItem(ChatService.isInCountryChat(player, paraguay)));
        });
        
        return gui;
    }

}
