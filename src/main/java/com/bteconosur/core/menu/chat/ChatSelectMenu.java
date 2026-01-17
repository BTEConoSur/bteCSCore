package com.bteconosur.core.menu.chat;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
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

        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        YamlConfiguration lang = ConfigHandler.getInstance().getLang();
        Player playerMenu = Player.getBTECSPlayer(player);
        String messageCountrySet = lang.getString("country-chat-set");
        String messageGlobalSet = lang.getString("global-chat-set");
        String messageNotePadSet = lang.getString("notepad-set");
        
        Pais arg = paisRegistry.getArgentina();
        gui.setItem(3,2, MenuUtils.getArgentinaHeadItem(ChatService.isInCountryChat(BTECSPlayer, arg)));
        gui.addSlotAction(3,2, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, arg);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageCountrySet.replace("%country%", arg.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            updateItems();
        });

        Pais chile = paisRegistry.getChile();
        gui.setItem(3,3, MenuUtils.getChileHeadItem(ChatService.isInCountryChat(BTECSPlayer, chile)));
        gui.addSlotAction(3,3, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, chile);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageCountrySet.replace("%country%", chile.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            updateItems();
        });

        Pais peru = paisRegistry.getPeru();
        gui.setItem(3,4, MenuUtils.getPeruHeadItem(ChatService.isInCountryChat(BTECSPlayer, peru)));
        gui.addSlotAction(3,4, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, peru);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageCountrySet.replace("%country%", peru.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            updateItems();
        });

        gui.setItem(2,4, MenuUtils.getGlobalChatHeadItem(ChatService.isInGlobalChat(BTECSPlayer)));
        gui.addSlotAction(2,4, event -> {
            ChatService.switchChatToGlobal(BTECSPlayer);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageGlobalSet.replace("%player%", BTECSPlayer.getNombre()), (String) null);
            updateItems();
        });

        gui.setItem(2,6, MenuUtils.getNotePadItem(ChatService.isInNotePad(BTECSPlayer)));
        gui.addSlotAction(2,6, event -> {
            ChatService.switchChatToNotePad(BTECSPlayer);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageNotePadSet.replace("%player%", BTECSPlayer.getNombre()), (String) null);
            updateItems();
        });

        Pais bolivia = paisRegistry.getBolivia();
        gui.setItem(3,6, MenuUtils.getBoliviaHeadItem(ChatService.isInCountryChat(BTECSPlayer, bolivia)));
        gui.addSlotAction(3,6, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, bolivia);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageCountrySet.replace("%country%", bolivia.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            updateItems();
        });

        Pais uruguay = paisRegistry.getUruguay();
        gui.setItem(3,7, MenuUtils.getUruguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, uruguay)));
        gui.addSlotAction(3,7, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, uruguay);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageCountrySet.replace("%country%", uruguay.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            updateItems();
        });

        Pais paraguay = paisRegistry.getParaguay();
        gui.setItem(3,8, MenuUtils.getParaguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, paraguay)));
        gui.addSlotAction(3,8, event -> {
            ChatService.switchChatToCountry(BTECSPlayer, paraguay);
            if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageCountrySet.replace("%country%", paraguay.getNombrePublico()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
            updateItems();
        });
        
        return gui;
    }

    private void updateItems() {
        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        gui.updateItem(3,2, MenuUtils.getArgentinaHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getArgentina())));
        gui.updateItem(3,3, MenuUtils.getChileHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getChile())));
        gui.updateItem(3,4, MenuUtils.getPeruHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getPeru())));
        gui.updateItem(2,4, MenuUtils.getGlobalChatHeadItem(ChatService.isInGlobalChat(BTECSPlayer)));
        gui.updateItem(2,6, MenuUtils.getNotePadItem(ChatService.isInNotePad(BTECSPlayer)));
        gui.updateItem(3,6, MenuUtils.getBoliviaHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getBolivia())));
        gui.updateItem(3,7, MenuUtils.getUruguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getUruguay())));
        gui.updateItem(3,8, MenuUtils.getParaguayHeadItem(ChatService.isInCountryChat(BTECSPlayer, paisRegistry.getParaguay())));
    }

    public void setBTECSPlayer(Player BTECSPlayer) {
        this.BTECSPlayer = BTECSPlayer;
    }

}
