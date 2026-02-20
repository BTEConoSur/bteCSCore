package com.bteconosur.core.menu.chat;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ChatSelectMenu extends Menu {

    private Player BTECSPlayer;
    private Language language;

    public ChatSelectMenu(Player player) {
    super(LanguageHandler.getText(player.getLanguage(), "gui-titles.chat-select"), 4, player);
        this.BTECSPlayer = player;
    }

    public ChatSelectMenu(Player player, Menu previousMenu) {
        super(LanguageHandler.getText(player.getLanguage(), "gui-titles.chat-select"), 4, player, previousMenu);
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

        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        Player playerMenu = Player.getBTECSPlayer(player);
        language = playerMenu.getLanguage();
        String messageCountrySet = LanguageHandler.getText(language, "country-chat.set");
        String messageGlobalSet = LanguageHandler.getText(language, "global-chat.set");
        String messageNotePadSet = LanguageHandler.getText(language, "notepad.set");
        
        Pais arg = paisRegistry.getArgentina();
        gui.setItem(3,2, MenuUtils.getArgentinaHeadItem(ChatService.isInCountryChat(BTECSPlayer, arg), language));
        gui.addSlotAction(3,2, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, arg);
            if (!BTECSPlayer.equals(playerMenu)) {
                String message = PlaceholderUtils.replaceMC(messageCountrySet, language, playerMenu);
                PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, arg), (String) null);
            }
            updateItems();
        });

        Pais chile = paisRegistry.getChile();
        gui.setItem(3,3, MenuUtils.getChileHeadItem(ChatService.isInCountryChat(BTECSPlayer, chile), language));
        gui.addSlotAction(3,3, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, chile);
            if (!BTECSPlayer.equals(playerMenu)) {  
                String message = PlaceholderUtils.replaceMC(messageCountrySet, language, BTECSPlayer);
                PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, chile), (String) null);
            }
            updateItems();
        });

        Pais peru = paisRegistry.getPeru();
        gui.setItem(3,4, MenuUtils.getPeruHeadItem(ChatService.isInCountryChat(BTECSPlayer, peru), language));
        gui.addSlotAction(3,4, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, peru);
            if (!BTECSPlayer.equals(playerMenu)) {  
                String message = PlaceholderUtils.replaceMC(messageCountrySet, language, BTECSPlayer);
                PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, peru), (String) null);
            }
            updateItems();
        });

        gui.setItem(2,4, MenuUtils.getGlobalChatHeadItem(ChatService.isInGlobalChat(BTECSPlayer), language));
        gui.addSlotAction(2,4, event -> {
            ChatService.switchChatToGlobal(BTECSPlayer);
            if (!BTECSPlayer.equals(playerMenu)) {  
                String message = PlaceholderUtils.replaceMC(messageGlobalSet, language, BTECSPlayer);
                PlayerLogger.info(playerMenu, message, (String) null);
            }   
            updateItems();
        });

        gui.setItem(2,6, MenuUtils.getNotePadItem(ChatService.isInNotePad(BTECSPlayer), language));
        gui.addSlotAction(2,6, event -> {
            ChatService.switchChatToNotePad(BTECSPlayer);
            if (!BTECSPlayer.equals(playerMenu)) {  
                String message = PlaceholderUtils.replaceMC(messageNotePadSet, language, BTECSPlayer);
                PlayerLogger.info(playerMenu, message, (String) null);
            }
            updateItems();
        });

        Pais bolivia = paisRegistry.getBolivia();
        gui.setItem(3,6, MenuUtils.getBoliviaHeadItem(ChatService.isInCountryChat(BTECSPlayer, bolivia), language));
        gui.addSlotAction(3,6, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, bolivia);
            if (!BTECSPlayer.equals(playerMenu)) {  
                String message = PlaceholderUtils.replaceMC(messageCountrySet, language, BTECSPlayer);
                PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, bolivia), (String) null);
            }   
            updateItems();
        });

        Pais uruguay = paisRegistry.getUruguay();
        gui.setItem(3,7, MenuUtils.getUruguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, uruguay), language));
        gui.addSlotAction(3,7, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, uruguay);
            if (!BTECSPlayer.equals(playerMenu)) {  
                String message = PlaceholderUtils.replaceMC(messageCountrySet, language, BTECSPlayer);
                PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, uruguay), (String) null);
            }
            updateItems();
        });

        Pais paraguay = paisRegistry.getParaguay();
        gui.setItem(3,8, MenuUtils.getParaguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, paraguay), language));
        gui.addSlotAction(3,8, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, paraguay);
            if (!BTECSPlayer.equals(playerMenu)) {  
                String message = PlaceholderUtils.replaceMC(messageCountrySet, language, BTECSPlayer);
                PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, paraguay), (String) null);
            }
            updateItems();
        });
        
        return gui;
    }

    private void updateItems() {
        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        gui.updateItem(3,2, MenuUtils.getArgentinaHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getArgentina()), language));
        gui.updateItem(3,3, MenuUtils.getChileHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getChile()), language));
        gui.updateItem(3,4, MenuUtils.getPeruHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getPeru()), language));
        gui.updateItem(2,4, MenuUtils.getGlobalChatHeadItem(ChatService.isInGlobalChat(BTECSPlayer), language));
        gui.updateItem(2,6, MenuUtils.getNotePadItem(ChatService.isInNotePad(BTECSPlayer), language));
        gui.updateItem(3,6, MenuUtils.getBoliviaHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getBolivia()), language));
        gui.updateItem(3,7, MenuUtils.getUruguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getUruguay()), language));
        gui.updateItem(3,8, MenuUtils.getParaguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getParaguay()), language));
    }

    public void setBTECSPlayer(Player BTECSPlayer) {
        this.BTECSPlayer = BTECSPlayer;
    }

}
