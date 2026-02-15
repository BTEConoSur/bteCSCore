package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectLeaveCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ProjectListMenu projectListMenu;

    public ProjectLeaveCommand() {
        super("leave", "Salir del proyecto en el que estás.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length > 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProjectManager projectManager = ProjectManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyectoFinal = null;

        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = pr.get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = pr.getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> memberProyectos = pr.getMemberOrLider(commandPlayer, proyectos);
            if (memberProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("not-a-member-here"), (String) null);
                return true;
            }

            if (memberProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-activos-list"), memberProyectos, (proyecto, event) -> {
                    if (!permissionManager.isMiembroOrLider(commandPlayer, proyecto)) {
                        String message = lang.getString("not-a-member").replace("%proyectoId%", proyecto.getId());   
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }

                    Boolean isLider = permissionManager.isLider(commandPlayer, proyecto);
                    if (isLider && permissionManager.hasMembers(proyecto)) {
                        String message = lang.getString("leader-cant-leave-project").replace("%proyectoId%", proyecto.getId());   
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    ConfirmationMenu confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.leave-project-confirm").replace("%proyectoId%", proyecto.getId()), commandPlayer, projectListMenu, confirmClick -> {
                        confirmClick.getWhoClicked().closeInventory();
                        projectManager.leaveProject(proyecto.getId(), commandPlayer.getUuid());
                        String notification;
                        if (isLider) notification = lang.getString("project-leader-left").replace("%proyectoId%", proyecto.getId());
                        else notification = lang.getString("project-member-left").replace("%proyectoId%", proyecto.getId());
                        PlayerLogger.info(commandPlayer, notification, (String) null);
                    });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = memberProyectos.iterator().next();
        }

        if (!permissionManager.isMiembroOrLider(commandPlayer, proyectoFinal)) {
            String message = lang.getString("not-a-member").replace("%proyectoId%", proyectoFinal.getId());   
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        Boolean isLider = permissionManager.isLider(commandPlayer, proyectoFinal);
        if (isLider && permissionManager.hasMembers(proyectoFinal)) {
            String message = lang.getString("leader-cant-leave-project").replace("%proyectoId%", proyectoFinal.getId());   
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }
        
        final String proyectoIdFinal = proyectoFinal.getId();
        ConfirmationMenu confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.leave-project-confirm").replace("%proyectoId%", proyectoIdFinal), commandPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
            projectManager.leaveProject(proyectoIdFinal, commandPlayer.getUuid());
            String notification;
            if (isLider) notification = lang.getString("project-leader-left").replace("%proyectoId%", proyectoIdFinal);
            else notification = lang.getString("project-member-left").replace("%proyectoId%", proyectoIdFinal);
            PlayerLogger.info(commandPlayer, notification, (String) null);
        }, cancelClick -> {
            cancelClick.getWhoClicked().closeInventory();
        });
        confirmationMenu.open();
        return true;
    }

}
