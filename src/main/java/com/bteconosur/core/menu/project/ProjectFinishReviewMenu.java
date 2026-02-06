package com.bteconosur.core.menu.project;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProjectFinishReviewMenu extends Menu {

    private final String proyectoId;
    private Player player;
    private Boolean liderPostulante;
    private String comentario;

    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public ProjectFinishReviewMenu(Player player, String proyectoId, String title, String comentario, Boolean liderPostulante) {
        super(title, 1, player);
        this.player = player;
        this.proyectoId = proyectoId;
        this.liderPostulante = liderPostulante;
        this.comentario = comentario;
    }

    public ProjectFinishReviewMenu(Player player, String proyectoId, String title, String comentario, Boolean liderPostulante, Menu previousMenu) {
        super(title, 1, player);
        this.player = player;
        this.proyectoId = proyectoId;
        this.liderPostulante = liderPostulante;
        this.comentario = comentario;
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
            getGui().close(player.getBukkitPlayer());
        });

        if (liderPostulante) {
            gui.setItem(3, MenuUtils.getPromoteItem());
            gui.addSlotAction(3, event -> {
                pm.acceptFinishRequest(proyectoId, player, comentario, true);
                PlayerLogger.info(player, lang.getString("project-finish-staff-accepted-and-promoted").replace("%proyectoId%", proyectoId), (String) null);
                getGui().close(player.getBukkitPlayer());
            });
        }

        gui.setItem(4, MenuUtils.getConfirmItem());
        gui.addSlotAction(4, event -> {
            pm.acceptFinishRequest(proyectoId, player, comentario, false);
            PlayerLogger.info(player, lang.getString("project-finish-staff-accepted").replace("%proyectoId%", proyectoId), (String) null);
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
