package com.bteconosur.core.menu.project;

import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.MenuUtils.PlayerContext;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.Estado;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProjectInfoMenu extends Menu {

    private Player BTECSPlayer;
    private Proyecto proyecto;

    private final ConfigHandler config = ConfigHandler.getInstance();
    private final YamlConfiguration lang = config.getLang();

    public ProjectInfoMenu(Player player, Proyecto proyecto, String title) {
        super(title, 5, player);
        this.BTECSPlayer = player;
        this.proyecto = proyecto;
    }

    public ProjectInfoMenu(Player player, Proyecto proyecto, Menu previousMenu, String title) {
        super(title, 5, player, previousMenu);
        this.BTECSPlayer = player;
        this.proyecto = proyecto;
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        ProjectManager pm = ProjectManager.getInstance();
        PlayerRegistry pr = PlayerRegistry.getInstance();
        gui.getFiller().fill(MenuUtils.getFillerItem());
        gui.setItem(2,2, MenuUtils.getProyecto(proyecto));

        Player lider = pm.getLider(proyecto);
        gui.setItem(2,3, MenuUtils.getPlayerItem(lider, pr.isOnline(lider.getUuid()), PlayerContext.LIDER));


        Set<Player> miembros = pm.getMembers(proyecto);
        gui.setItem(2,4, MenuUtils.getMembersItem(miembros.size()));
        gui.addSlotAction(2,4, event -> {
            MemberListMenu memberListMenu = new MemberListMenu(BTECSPlayer, proyecto, lang.getString("gui-titles.project-members").replace("%proyectoId%", proyecto.getId()), miembros, this, true);
            memberListMenu.open();
        });

        if (proyecto.getEstado() == Estado.ABANDONADO) {
            gui.setItem(2,5, MenuUtils.getClaimProjectItem());
        }
  
        return gui;
    }

}
