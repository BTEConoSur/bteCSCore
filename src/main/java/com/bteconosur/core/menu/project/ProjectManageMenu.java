package com.bteconosur.core.menu.project;

import java.util.Set;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
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
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProjectManageMenu extends Menu {

    private Player BTECSPlayer;
    private Proyecto proyecto;
    private Language language;

    public ProjectManageMenu(Player player, Proyecto proyecto, String title) {
        super(title, 5, player);
        this.BTECSPlayer = player;
        this.proyecto = proyecto;
        this.language = player.getLanguage();
    }

    public ProjectManageMenu(Player player, Proyecto proyecto, Menu previousMenu, String title) {
        super(title, 5, player, previousMenu);
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
        PermissionManager permissionManager = PermissionManager.getInstance();
        InteractionRegistry ir = InteractionRegistry.getInstance();
        PlayerRegistry pr = PlayerRegistry.getInstance();
        ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
        Pais pais = proyecto.getPais();
        gui.getFiller().fill(MenuUtils.getFillerItem());
        gui.setItem(2,2, MenuUtils.getProyecto(proyecto, language));

        Player lider = pm.getLider(proyecto);
        gui.setItem(2,3, MenuUtils.getPlayerItem(lider, pr.isOnline(lider == null ? null : lider.getUuid()), PlayerContext.LIDER, language));

        if (BTECSPlayer.equals(lider) || permissionManager.isManager(BTECSPlayer, pais)) {
            gui.setItem(4,8, MenuUtils.getNotificationsItem(ir.countJoinRequests(proyecto.getId()), language));
            gui.addSlotAction(4,8, event -> {
                String title = LanguageHandler.replaceMC("gui-titles.join-request-list", language, proyecto);
                new JoinRequestListMenu(BTECSPlayer, title, proyecto, this).open();
            });

            gui.setItem(4,2, MenuUtils.getSetNameDescription(language));

            if (proyecto.getEstado() == Estado.ACTIVO || proyecto.getEstado() == Estado.EDITANDO) {
                gui.setItem(4,4, MenuUtils.getFinishProjectItem(language));
                gui.addSlotAction(4,4, event -> {
                    ConfirmationMenu confirmationMenu = new ConfirmationMenu(LanguageHandler.replaceMC("gui-titles.finish-project-confirm", language, proyecto), BTECSPlayer, this, confirmClick -> {
                        confirmClick.getWhoClicked().closeInventory();
                        if (proyecto.getEstado() == Estado.EDITANDO) {
                            ProjectManager.getInstance().createFinishEditRequest(proyecto.getId(), BTECSPlayer.getUuid());
                            PlayerLogger.info(BTECSPlayer, LanguageHandler.replaceMC("project.edit.finish.request.success", language, proyecto), (String) null);
                        } else {
                            ProjectManager.getInstance().createFinishRequest(proyecto.getId(), BTECSPlayer.getUuid());
                            PlayerLogger.info(BTECSPlayer, LanguageHandler.replaceMC("project.finish.request.success", language, proyecto), (String) null);
                        }
                    });
                confirmationMenu.open();
                });
            }
            
            if (proyecto.getEstado() == Estado.ABANDONADO || proyecto.getEstado() == Estado.ACTIVO || proyecto.getEstado() == Estado.EDITANDO) {
                gui.setItem(4,6, MenuUtils.getLiderTransferItem(language));
                gui.addSlotAction(4,6, event -> {
                    if (proyecto.getEstado() == Estado.ABANDONADO) {
                        PlayerListMenu playerListMenu = new PlayerListMenu(BTECSPlayer, LanguageHandler.replaceMC("gui-titles.select-leader", language, proyecto), (player, clickEvent) -> {
                            clickEvent.getWhoClicked().closeInventory();
                            ProjectManager.getInstance().switchLeader(proyecto.getId(), player.getUuid(), BTECSPlayer.getUuid());
                            String successMessage = LanguageHandler.replaceMC("project.leader.switch.success", language, player, proyecto);   
                            PlayerLogger.info(BTECSPlayer, successMessage, (String) null);
                        }, this);
                        playerListMenu.open();
                        return;
                    }
                    Set<Player> miembros = pm.getMembers(proyecto);
                    PlayerListMenu playerListMenu = new PlayerListMenu(BTECSPlayer, LanguageHandler.replaceMC("gui-titles.select-leader", language, proyecto), miembros, false, MenuUtils.PlayerContext.MIEMBRO, (player, clickEvent) -> {
                        clickEvent.getWhoClicked().closeInventory();
                        if (!permissionManager.isMiembro(player, proyecto)) {
                            String message = LanguageHandler.replaceMC("project.member.not-member", language, proyecto);
                            PlayerLogger.error(BTECSPlayer, message, (String) null);
                             
                            return;
                        }
                        if (!permissionManager.isManager(BTECSPlayer, proyecto.getPais())) {
                            int activeProjects = proyectoRegistry.getCounts(player)[1];
                            int maxActiveProjects = player.getTipoUsuario().getCantProyecSim();
                            if (activeProjects >= maxActiveProjects) {
                                String message = LanguageHandler.replaceMC("project.leader.max-active-projects-transfer", language, player).replace("%maxProjects%", String.valueOf(maxActiveProjects)).replace("%currentProjects%", String.valueOf(activeProjects)).replace("%player%", player.getNombre());
                                PlayerLogger.error(BTECSPlayer, message, (String) null);
                                return;
                            }
                        }
                        ProjectManager.getInstance().switchLeader(proyecto.getId(), player.getUuid(), BTECSPlayer.getUuid());
                        String successMessage = LanguageHandler.replaceMC("project.leader.switch.success", language, player, proyecto);   
                        PlayerLogger.info(BTECSPlayer, successMessage, (String) null);
                    }, this);
                    playerListMenu.open();
                });
            }
        }

        gui.setItem(2,8, MenuUtils.getLeaveProjectItem(language));
        gui.addSlotAction(2,8, event -> {
            ConfirmationMenu confirmationMenu = new ConfirmationMenu(LanguageHandler.replaceMC("gui-titles.leave-project-confirm", language, proyecto), BTECSPlayer, this, confirmClick -> {
                event.getWhoClicked().closeInventory();
                if (!permissionManager.isMiembroOrLider(BTECSPlayer, proyecto)) {
                    String message = LanguageHandler.replaceMC("project.member.not-a-member", language, proyecto); //TODO: Revisar not-member y not-a-member
                    PlayerLogger.error(BTECSPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }

                Boolean isLider = permissionManager.isLider(BTECSPlayer, proyecto);
                if (isLider && permissionManager.hasMembers(proyecto)) {
                    String message = LanguageHandler.replaceMC("project.leader.leave.cant-leave", language, proyecto);   
                    PlayerLogger.error(BTECSPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }
                pm.leaveProject(proyecto.getId(), BTECSPlayer.getUuid());
                String notification;
                if (isLider) notification = LanguageHandler.replaceMC("project.leader.leave.success", language, proyecto);
                else notification = LanguageHandler.replaceMC("project.member.leave.success", language, proyecto);
                PlayerLogger.info(BTECSPlayer, notification, (String) null);
            });
            confirmationMenu.open();
        });


        Set<Player> miembros = pm.getMembers(proyecto);
        gui.setItem(2,4, MenuUtils.getMembersItem(proyecto, language));
        gui.addSlotAction(2,4, event -> {
            MemberListMenu memberListMenu = new MemberListMenu(BTECSPlayer, proyecto, LanguageHandler.replaceMC("gui-titles.project-members", language, proyecto), miembros, this, false);
            memberListMenu.open();
        });
        return gui;
    }

}
