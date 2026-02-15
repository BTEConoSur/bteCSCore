package com.bteconosur.core.menu.project;

import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.menu.player.PlayerListMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.MenuUtils.PlayerContext;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.Estado;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProjectManageMenu extends Menu {

    private Player BTECSPlayer;
    private Proyecto proyecto;

    private final ConfigHandler config = ConfigHandler.getInstance();
    private final YamlConfiguration lang = config.getLang();

    public ProjectManageMenu(Player player, Proyecto proyecto, String title) {
        super(title, 5, player);
        this.BTECSPlayer = player;
        this.proyecto = proyecto;
    }

    public ProjectManageMenu(Player player, Proyecto proyecto, Menu previousMenu, String title) {
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
        PermissionManager permissionManager = PermissionManager.getInstance();
        InteractionRegistry ir = InteractionRegistry.getInstance();
        PlayerRegistry pr = PlayerRegistry.getInstance();
        Pais pais = proyecto.getPais();
        gui.getFiller().fill(MenuUtils.getFillerItem());
        gui.setItem(2,2, MenuUtils.getProyecto(proyecto));

        Player lider = pm.getLider(proyecto);
        gui.setItem(2,3, MenuUtils.getPlayerItem(lider, pr.isOnline(lider.getUuid()), PlayerContext.LIDER));

        if (BTECSPlayer.equals(lider) || permissionManager.isManager(BTECSPlayer, pais)) {
            gui.setItem(4,8, MenuUtils.getNotificationsItem(ir.countJoinRequests(proyecto.getId())));
            gui.addSlotAction(4,8, event -> {
                String title = lang.getString("gui-titles.join-request-list").replace("%proyectoId%", proyecto.getId());
                new JoinRequestListMenu(BTECSPlayer, title, proyecto, this).open();
            });

            gui.setItem(4,2, MenuUtils.getSetNameDescription());

            if (proyecto.getEstado() == Estado.ACTIVO || proyecto.getEstado() == Estado.EDITANDO) {
                gui.setItem(4,4, MenuUtils.getFinishProjectItem());
                gui.addSlotAction(4,4, event -> {
                    ConfirmationMenu confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.finish-project-confirm").replace("%proyectoId%", proyecto.getId()), BTECSPlayer, this, confirmClick -> {
                        confirmClick.getWhoClicked().closeInventory();
                        if (proyecto.getEstado() == Estado.EDITANDO) {
                            ProjectManager.getInstance().createFinishEditRequest(proyecto.getId(), BTECSPlayer.getUuid());
                            PlayerLogger.info(BTECSPlayer, lang.getString("project-finish-edit-request-success").replace("%proyectoId%", proyecto.getId()), (String) null);
                        } else {
                            ProjectManager.getInstance().createFinishRequest(proyecto.getId(), BTECSPlayer.getUuid());
                            PlayerLogger.info(BTECSPlayer, lang.getString("project-finish-request-success").replace("%proyectoId%", proyecto.getId()), (String) null);
                        }
                    });
                confirmationMenu.open();
                });

                gui.setItem(4,6, MenuUtils.getLiderTransferItem());
                gui.addSlotAction(4,6, event -> {
                    Set<Player> miembros = pm.getMembers(proyecto);
                    PlayerListMenu playerListMenu = new PlayerListMenu(BTECSPlayer, lang.getString("gui-titles.select-leader").replace("%proyectoId%", proyecto.getId()), miembros, false, MenuUtils.PlayerContext.MIEMBRO, (player, clickEvent) -> {
                        clickEvent.getWhoClicked().closeInventory();
                        if (!permissionManager.isMiembro(player, proyecto)) {
                            String message = lang.getString("project-not-member").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
                            PlayerLogger.error(BTECSPlayer, message, (String) null);
                             
                            return;
                        }
                        ProjectManager.getInstance().switchLeader(proyecto.getId(), player.getUuid(), BTECSPlayer.getUuid());
                        String successMessage = lang.getString("project-leader-switched-success").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());   
                        PlayerLogger.info(BTECSPlayer, successMessage, (String) null);
                    }, this);
                    playerListMenu.open();
                });
            }
        }

        gui.setItem(2,8, MenuUtils.getLeaveProjectItem());
        gui.addSlotAction(2,8, event -> {
            ConfirmationMenu confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.leave-project-confirm").replace("%proyectoId%", proyecto.getId()), BTECSPlayer, this, confirmClick -> {
                event.getWhoClicked().closeInventory();
                if (!permissionManager.isMiembroOrLider(BTECSPlayer, proyecto)) {
                    String message = lang.getString("not-a-member").replace("%proyectoId%", proyecto.getId());   
                    PlayerLogger.error(BTECSPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }

                Boolean isLider = permissionManager.isLider(BTECSPlayer, proyecto);
                if (isLider && permissionManager.hasMembers(proyecto)) {
                    String message = lang.getString("leader-cant-leave-project").replace("%proyectoId%", proyecto.getId());   
                    PlayerLogger.error(BTECSPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }
                pm.leaveProject(proyecto.getId(), BTECSPlayer.getUuid());
                String notification;
                if (isLider) notification = lang.getString("project-leader-left").replace("%proyectoId%", proyecto.getId());
                else notification = lang.getString("project-member-left").replace("%proyectoId%", proyecto.getId());
                PlayerLogger.info(BTECSPlayer, notification, (String) null);
            });
            confirmationMenu.open();
        });


        Set<Player> miembros = pm.getMembers(proyecto);
        gui.setItem(2,4, MenuUtils.getMembersItem(miembros.size()));
        gui.addSlotAction(2,4, event -> {
            MemberListMenu memberListMenu = new MemberListMenu(BTECSPlayer, proyecto, lang.getString("gui-titles.project-members").replace("%proyectoId%", proyecto.getId()), miembros, this);
            memberListMenu.open();
        });
        return gui;
    }

}
