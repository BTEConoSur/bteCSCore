package com.bteconosur.core.menu.project;

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
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.TipoUsuarioRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProjectPromoteMenu extends Menu {
    
    private Player BTECSPlayer;
    private ConfirmationMenu confirmationMenu;
    private Language language;

    public ProjectPromoteMenu(Player player, String title) {
        super(title, 3, player);
        this.BTECSPlayer = player;
        this.language = player.getLanguage();
    }

    public ProjectPromoteMenu(Player player, Menu previousMenu, String title) {
        super(title, 3, player, previousMenu);
        this.BTECSPlayer = player;
        this.language = player.getLanguage();
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        String messageSet = LanguageHandler.getText(language, "tipo.set");
        Player playerMenu = Player.getBTECSPlayer(player);

        gui.getFiller().fill(MenuUtils.getFillerItem());

        TipoUsuarioRegistry tur = TipoUsuarioRegistry.getInstance(); 
        PermissionManager pm = PermissionManager.getInstance();
        TipoUsuario visita = tur.getVisita();
        gui.setItem(2,3, MenuUtils.getTipoUsuario(visita, pm.isTipoUsuario(BTECSPlayer, visita), language));
        gui.addSlotAction(2,3, event -> {
            if (pm.isTipoUsuario(BTECSPlayer, visita)) return;
            String title = LanguageHandler.getText(language, "gui-titles.tipo-confirm");
            confirmationMenu = new ConfirmationMenu(title, player, this, confirmClick -> {
                pm.switchTipoUsuario(BTECSPlayer, visita);
                PlayerLogger.info(BTECSPlayer, LanguageHandler.replaceMC("tipo.switch", BTECSPlayer.getLanguage(), visita), ChatUtil.getDsTipoUsuarioSwitched(visita, BTECSPlayer.getLanguage()));
                if (!BTECSPlayer.equals(playerMenu)) {
                    String message = PlaceholderUtils.replaceMC(messageSet, language, visita);
                    PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, BTECSPlayer), (String) null);
                }
                String countryLog = LanguageHandler.replaceDS("tipo.promote-staff-log", Language.getDefault(), playerMenu, BTECSPlayer);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), visita);
                DiscordLogger.staffLog(countryLog);
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        TipoUsuario postulante = tur.getPostulante();
        gui.setItem(2,5, MenuUtils.getTipoUsuario(postulante, pm.isTipoUsuario(BTECSPlayer, postulante), language));
        gui.addSlotAction(2,5, event -> {
            if (pm.isTipoUsuario(BTECSPlayer, postulante)) return;
            String title = LanguageHandler.getText(language, "gui-titles.tipo-confirm");
            confirmationMenu = new ConfirmationMenu(title, player, this, confirmClick -> {
                pm.switchTipoUsuario(BTECSPlayer, postulante);
                PlayerLogger.info(BTECSPlayer, LanguageHandler.replaceMC("tipo.switch", BTECSPlayer.getLanguage(), postulante), ChatUtil.getDsTipoUsuarioSwitched(postulante, BTECSPlayer.getLanguage()));
                if (!BTECSPlayer.equals(playerMenu)) {
                    String message = PlaceholderUtils.replaceMC(messageSet, language, postulante);
                    PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, BTECSPlayer), (String) null);
                }
                String countryLog = LanguageHandler.replaceDS("tipo.promote-staff-log", Language.getDefault(), playerMenu, BTECSPlayer);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), postulante);
                DiscordLogger.staffLog(countryLog);
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        TipoUsuario constructor = tur.getConstructor();
        gui.setItem(2,7, MenuUtils.getTipoUsuario(constructor, pm.isTipoUsuario(BTECSPlayer, constructor), language));
        gui.addSlotAction(2,7, event -> {
            if (pm.isTipoUsuario(BTECSPlayer, constructor)) return;
            String title = LanguageHandler.getText(language, "gui-titles.tipo-confirm");
            confirmationMenu = new ConfirmationMenu(title, player, this, confirmClick -> {
                pm.switchTipoUsuario(BTECSPlayer, constructor);
                PlayerLogger.info(BTECSPlayer, LanguageHandler.replaceMC("tipo.switch", BTECSPlayer.getLanguage(), constructor), ChatUtil.getDsTipoUsuarioSwitched(constructor, BTECSPlayer.getLanguage()));
                if (!BTECSPlayer.equals(playerMenu)) {
                    String message = PlaceholderUtils.replaceMC(messageSet, language, constructor);
                    PlayerLogger.info(playerMenu, PlaceholderUtils.replaceMC(message, language, BTECSPlayer), (String) null);
                }
                String countryLog = LanguageHandler.replaceDS("tipo.promote.promote-staff-log", Language.getDefault(), playerMenu, BTECSPlayer);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), constructor);
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
