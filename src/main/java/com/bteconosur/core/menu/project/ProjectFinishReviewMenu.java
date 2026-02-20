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

public class ProjectFinishReviewMenu extends Menu {

    private final Proyecto proyecto;
    private Player player;
    private Boolean liderPostulante;
    private String comentario;
    private Language language;

    public ProjectFinishReviewMenu(Player player, Proyecto proyecto, String title, String comentario, Boolean liderPostulante) {
        super(title, 1, player);
        this.player = player;
        this.proyecto = proyecto;
        this.liderPostulante = liderPostulante;
        this.comentario = comentario;
        this.language = player.getLanguage();
    }

    public ProjectFinishReviewMenu(Player player, Proyecto proyecto, String title, String comentario, Boolean liderPostulante, Menu previousMenu) {
        super(title, 1, player);
        this.player = player;
        this.proyecto = proyecto;
        this.liderPostulante = liderPostulante;
        this.comentario = comentario;
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

        if (liderPostulante) {
            gui.setItem(3, MenuUtils.getPromoteItem(language));
            gui.addSlotAction(3, event -> {
                pm.acceptFinishRequest(proyecto.getId(), player, comentario, true);
                PlayerLogger.info(player, LanguageHandler.replaceMC("project.finish.accept.succes-promote", language, proyecto), (String) null);
                getGui().close(player.getBukkitPlayer());
            });
        }

        gui.setItem(4, MenuUtils.getConfirmItem(language));
        gui.addSlotAction(4, event -> {
            pm.acceptFinishRequest(proyecto.getId(), player, comentario, false);
            PlayerLogger.info(player, LanguageHandler.replaceMC("project.finish.accept.success", language, proyecto), (String) null);
            getGui().close(player.getBukkitPlayer());
        });
        
        return gui;
    }

    @Override
    public void open() {
        if (super.player == null || !super.player.isOnline()) return;
        if (gui == null) gui = createGui();
         
        gui.open(super.player);
    }   

}
