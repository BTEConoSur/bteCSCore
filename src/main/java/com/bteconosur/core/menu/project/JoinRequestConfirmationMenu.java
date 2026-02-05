package com.bteconosur.core.menu.project;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
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

    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public JoinRequestConfirmationMenu(Player player, String title, Proyecto proyecto, Player requestPlayer, Long interactionId, Menu previousMenu) {
        super(title, 1, player, previousMenu);
        this.proyecto = proyecto;
        this.requestPlayer = requestPlayer;
        this.interactionId = interactionId;
        this.previousMenu = previousMenu;
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

        gui.setItem(0, MenuUtils.getCancelItem());
        gui.addSlotAction(0, event -> {
            previousMenu.open();
        });     

        gui.setItem(3, MenuUtils.getRejectItem());
        gui.addSlotAction(3, event -> {
            pm.rejectJoinRequest(proyecto.getId(), requestPlayer.getUuid(), interactionId, player.getUniqueId());
            PlayerLogger.info(player, lang.getString("project-join-rejected-lider").replace("%player%", requestPlayer.getNombre()).replace("%proyectoId%", proyecto.getId()), (String) null);
            getGui().close(player);
        });

        gui.setItem(4, MenuUtils.getAcceptItem());
        gui.addSlotAction(4, event -> {
            pm.acceptJoinRequest(proyecto.getId(), requestPlayer.getUuid(), interactionId, player.getUniqueId());
            PlayerLogger.info(player, lang.getString("project-join-accepted-lider").replace("%player%", requestPlayer.getNombre()).replace("%proyectoId%", proyecto.getId()), (String) null);
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
