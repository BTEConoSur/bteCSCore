package com.bteconosur.core.menu.project;


import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;

import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class JoinRequestConfirmationMenu extends Menu {

    private final Proyecto proyecto;
    private final Player requestPlayer;
    private final Long interactionId;
    private Language language;

    public JoinRequestConfirmationMenu(Player player, String title, Proyecto proyecto, Player requestPlayer, Long interactionId, Menu previousMenu) {
        super(title, 1, player, previousMenu);
        this.proyecto = proyecto;
        this.requestPlayer = requestPlayer;
        this.interactionId = interactionId;
        this.previousMenu = previousMenu;
        this.language = player.getLanguage();
    }

    @Override
    protected BaseGui createGui() {
        ProjectManager pm = ProjectManager.getInstance();
        Gui gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .type(GuiType.HOPPER)
            .disableAllInteractions()
            .create();

        gui.getFiller().fill(MenuUtils.getFillerItem());

        gui.setItem(0, MenuUtils.getCancelItem(language));
        gui.addSlotAction(0, event -> {
            previousMenu.open();
        });     

        gui.setItem(3, MenuUtils.getRejectItem(language));
        gui.addSlotAction(3, event -> {
            pm.rejectJoinRequest(proyecto.getId(), requestPlayer.getUuid(), interactionId, player.getUniqueId());
            PlayerLogger.info(player, LanguageHandler.replaceMC("project.join.reject.success", language, requestPlayer, proyecto), (String) null);
            getGui().close(player);
        });

        gui.setItem(4, MenuUtils.getAcceptItem(language));
        gui.addSlotAction(4, event -> {
            pm.acceptJoinRequest(proyecto.getId(), requestPlayer.getUuid(), interactionId, player.getUniqueId());
            PlayerLogger.info(player, LanguageHandler.replaceMC("project.join.accept.success", language, requestPlayer, proyecto), (String) null);
            getGui().close(player);
        });
        
        return gui;
    }

    @Override
    public void open() {
        if (player == null || !player.isOnline()) return;
        if (gui == null) gui = createGui();
         
        gui.open(player);
    }   
    
}
