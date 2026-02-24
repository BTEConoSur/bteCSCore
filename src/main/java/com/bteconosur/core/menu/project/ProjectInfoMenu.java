package com.bteconosur.core.menu.project;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.locationtech.jts.geom.Point;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.MenuUtils.PlayerContext;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.world.WorldManager;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProjectInfoMenu extends Menu {

    private Player BTECSPlayer;
    private Proyecto proyecto;
    private Language language;

    public ProjectInfoMenu(Player player, Proyecto proyecto, String title) {
        super(title, 3, player);
        this.language = player.getLanguage();
        this.BTECSPlayer = player;
        this.proyecto = proyecto;
    }

    public ProjectInfoMenu(Player player, Proyecto proyecto, Menu previousMenu, String title) {
        super(title, 3, player, previousMenu);
        this.BTECSPlayer = player;
        this.proyecto = proyecto;
        this.language = player.getLanguage();   
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
        gui.setItem(2,2, MenuUtils.getProyecto(proyecto, language));

        Player lider = pm.getLider(proyecto);
        
        gui.setItem(2,3, MenuUtils.getPlayerItem(lider, pr.isOnline(lider == null ? null : lider.getUuid()), PlayerContext.LIDER, language));

        Set<Player> miembros = pm.getMembers(proyecto);
        gui.setItem(2,4, MenuUtils.getMembersItem(proyecto, language));
        gui.addSlotAction(2,4, event -> {
            if (miembros.size() == 0) return;
            String title = LanguageHandler.replaceMC("gui-titles.project-members", language, proyecto);
            MemberListMenu memberListMenu = new MemberListMenu(BTECSPlayer, proyecto, title, miembros, this, true);
            memberListMenu.open();
        });

        if (proyecto.getEstado() == Estado.ABANDONADO) {
            gui.setItem(2,8, MenuUtils.getClaimProjectItem(language));
            gui.addSlotAction(2,8, event -> {
                String title = LanguageHandler.replaceMC("gui-titles.claim-project-confirm", language, proyecto);
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(title, BTECSPlayer, this, confirmClick -> {
                    event.getWhoClicked().closeInventory();
                    ProjectManager projectManager = ProjectManager.getInstance();
                    projectManager.claim(proyecto.getId(), BTECSPlayer.getUuid());
                    PlayerLogger.info(BTECSPlayer, LanguageHandler.replaceMC("project.claim.success", language, proyecto), (String) null);
                });
                confirmationMenu.open();
            });
        }

        gui.setItem(2,6, MenuUtils.getTeleportItem(language));
        gui.addSlotAction(2,6, event -> {
            event.getWhoClicked().closeInventory();
            Point centroid = proyecto.getPoligono().getCentroid();
            double x = Math.floor(centroid.getX());
            double z = Math.floor(centroid.getY());
            
            World world = WorldManager.getInstance().getBTEWorld().getLabelWorld(x, z).getBukkitWorld();
            int highestY = world.getHighestBlockYAt((int) x, (int) z);
            
            org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) event.getWhoClicked();
            Location tpLocation = new Location(world, x + 0.5, highestY + 1, z + 0.5, bukkitPlayer.getLocation().getYaw(), bukkitPlayer.getLocation().getPitch());
            bukkitPlayer.teleport(tpLocation);
            
            PlayerLogger.info(BTECSPlayer, LanguageHandler.replaceMC("project.tp-success", language, proyecto), (String) null);
        });
  
        return gui;
    }

}
