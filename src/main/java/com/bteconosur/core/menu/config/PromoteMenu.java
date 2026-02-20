package com.bteconosur.core.menu.config;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PromoteMenu extends Menu {

    private Player BTECSPlayer;
    private Language language;
    private ConfirmationMenu confirmationMenu;

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

        language = Player.getBTECSPlayer(player).getLanguage();
        String messageSwitch = LanguageHandler.getText(language, "rango.switch");
        String messageSet = LanguageHandler.getText(language, "rango.set");
        Player playerMenu = Player.getBTECSPlayer(player);

        gui.getFiller().fill(MenuUtils.getFillerItem());

        RangoUsuarioRegistry rur = RangoUsuarioRegistry.getInstance(); 
        PermissionManager pm = PermissionManager.getInstance();
        RangoUsuario admin = rur.getAdmin();
        gui.setItem(2,2, MenuUtils.getRangoUsuario(admin, pm.isAdmin(BTECSPlayer), language));
        gui.addSlotAction(2,2, event -> {
            if (pm.isAdmin(BTECSPlayer)) return;
            String confirmTitle = LanguageHandler.getText(language, "gui-titles.rango-confirm");
            confirmationMenu = new ConfirmationMenu(confirmTitle, player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, admin);
                PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), admin), ChatUtil.getDsRangoUsuarioSwitched(admin, BTECSPlayer.getLanguage()));
                if (!BTECSPlayer.equals(playerMenu)) {
                    String message = PlaceholderUtils.replaceMC(messageSet, BTECSPlayer.getLanguage(), admin);
                    PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, playerMenu.getLanguage(), BTECSPlayer), (String) null);
                }
                String countryLog = LanguageHandler.replaceDS("rango.promote-staff-log", Language.getDefault(), playerMenu, BTECSPlayer);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), admin);
                DiscordLogger.staffLog(countryLog);
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        RangoUsuario mod = rur.getMod();
        gui.setItem(2,3, MenuUtils.getRangoUsuario(mod, pm.isRangoUsuario(BTECSPlayer, mod), language));
        gui.addSlotAction(2,3, event -> {
            if (pm.isRangoUsuario(BTECSPlayer, mod)) return;
            String confirmTitle = LanguageHandler.getText(language, "gui-titles.rango-confirm");
            confirmationMenu = new ConfirmationMenu(confirmTitle, player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, mod);
                PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), mod), ChatUtil.getDsRangoUsuarioSwitched(mod, BTECSPlayer.getLanguage()));
                if (!BTECSPlayer.equals(playerMenu)) {
                    String message = PlaceholderUtils.replaceMC(messageSet, playerMenu.getLanguage(), mod);
                    PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, playerMenu.getLanguage(), BTECSPlayer), (String) null);
                }
                String countryLog = LanguageHandler.replaceDS("rango.promote-staff-log", Language.getDefault(), playerMenu, BTECSPlayer);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), mod);
                DiscordLogger.staffLog(countryLog);
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        RangoUsuario normal = rur.getNormal();
        gui.setItem(2,5, MenuUtils.getRangoUsuario(normal, pm.isRangoUsuario(BTECSPlayer, normal), language));
        gui.addSlotAction(2,5, event -> {
            if (pm.isRangoUsuario(BTECSPlayer, normal)) return;
            String confirmTitle = LanguageHandler.getText(language, "gui-titles.rango-confirm");
            confirmationMenu = new ConfirmationMenu(confirmTitle, player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, normal);
                PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), normal), ChatUtil.getDsRangoUsuarioSwitched(normal, BTECSPlayer.getLanguage()));
                if (!BTECSPlayer.equals(playerMenu)) {
                    String message = PlaceholderUtils.replaceMC(messageSet, playerMenu.getLanguage(), normal);
                    PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, playerMenu.getLanguage(), BTECSPlayer), (String) null);
                }
                String countryLog = LanguageHandler.replaceDS("rango.promote-staff-log", Language.getDefault(), playerMenu, BTECSPlayer);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), normal);
                DiscordLogger.staffLog(countryLog);
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        RangoUsuario influencer = rur.getInfluencer();
        gui.setItem(2,7, MenuUtils.getRangoUsuario(influencer, pm.isRangoUsuario(BTECSPlayer, influencer), language));
        gui.addSlotAction(2,7, event -> {
            if (pm.isRangoUsuario(BTECSPlayer, influencer)) return;
            String confirmTitle = LanguageHandler.getText(language, "gui-titles.rango-confirm");
            confirmationMenu = new ConfirmationMenu(confirmTitle, player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, influencer);
                PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), influencer), ChatUtil.getDsRangoUsuarioSwitched(influencer, BTECSPlayer.getLanguage()));
                if (!BTECSPlayer.equals(playerMenu)) {
                    String message = PlaceholderUtils.replaceMC(messageSet, playerMenu.getLanguage(), influencer);
                    PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, playerMenu.getLanguage(), BTECSPlayer), (String) null);
                }
                String countryLog = LanguageHandler.replaceDS("rango.promote-staff-log", Language.getDefault(), playerMenu, BTECSPlayer);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), influencer);
                DiscordLogger.staffLog(countryLog);
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        RangoUsuario donador = rur.getDonador();
        gui.setItem(2,8, MenuUtils.getRangoUsuario(donador, pm.isRangoUsuario(BTECSPlayer, donador), language));
        gui.addSlotAction(2,8, event -> {
            if (pm.isRangoUsuario(BTECSPlayer, donador)) return;
            String confirmTitle = LanguageHandler.getText(language, "gui-titles.rango-confirm");
            confirmationMenu = new ConfirmationMenu(confirmTitle, player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, donador);
                PlayerLogger.info(BTECSPlayer, PlaceholderUtils.replaceMC(messageSwitch, BTECSPlayer.getLanguage(), donador), ChatUtil.getDsRangoUsuarioSwitched(donador, BTECSPlayer.getLanguage()));
                if (!BTECSPlayer.equals(playerMenu)) {
                    String message = PlaceholderUtils.replaceMC(messageSet, playerMenu.getLanguage(), donador);
                    PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, playerMenu.getLanguage(), BTECSPlayer), (String) null);
                }
                String countryLog = LanguageHandler.replaceDS("rango.promote-staff-log", Language.getDefault(), playerMenu, BTECSPlayer);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), donador);
                DiscordLogger.staffLog(countryLog);
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });
        
        return gui;
    }

    public void setBTECSPlayer(Player BTECSPlayer) {
        this.BTECSPlayer = BTECSPlayer;
    }

}
