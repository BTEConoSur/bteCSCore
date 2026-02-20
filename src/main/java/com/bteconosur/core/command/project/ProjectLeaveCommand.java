package com.bteconosur.core.command.project;

import java.util.Set;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectLeaveCommand extends BaseCommand {

    private ProjectListMenu projectListMenu;

    public ProjectLeaveCommand() {
        super("leave", "Salir del proyecto en el que estás.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length > 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProjectManager projectManager = ProjectManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyectoFinal = null;

        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = pr.get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-found-id", language, proyectoFinal), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = pr.getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> memberProyectos = pr.getMemberOrLider(commandPlayer, proyectos);
            if (memberProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.member.not-member-here"), (String) null);
                return true;
            }

            if (memberProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-activos-list"), memberProyectos, (proyecto, event) -> {
                    if (!permissionManager.isMiembroOrLider(commandPlayer, proyecto)) {
                        String message = LanguageHandler.replaceMC("project.member.not-a-member", language, proyecto);   
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }

                    Boolean isLider = permissionManager.isLider(commandPlayer, proyecto);
                    if (isLider && permissionManager.hasMembers(proyecto)) {
                        String message = LanguageHandler.getText(language, "project.leader.leave.cant-leave");   
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    String title = LanguageHandler.replaceMC("gui-titles.leave-project-confirm", language, proyecto);
                    ConfirmationMenu confirmationMenu = new ConfirmationMenu(title, commandPlayer, projectListMenu, confirmClick -> {
                        confirmClick.getWhoClicked().closeInventory();
                        projectManager.leaveProject(proyecto.getId(), commandPlayer.getUuid());
                        String notification;
                        if (isLider) notification = LanguageHandler.replaceMC("project.leader.leave.success", language, proyecto);
                        else notification = LanguageHandler.replaceMC("project.member.leave.success", language, proyecto);
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
            String message = LanguageHandler.replaceMC("project.member.not-a-member", language, proyectoFinal);   
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        Boolean isLider = permissionManager.isLider(commandPlayer, proyectoFinal);
        if (isLider && permissionManager.hasMembers(proyectoFinal)) {
            String message = LanguageHandler.replaceMC("project.leader.leave.cant-leave", language, proyectoFinal);   
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }
        
        final Proyecto proyectoFinalFinal = proyectoFinal;
        String title = LanguageHandler.replaceMC("gui-titles.leave-project-confirm", language, proyectoFinalFinal);
        ConfirmationMenu confirmationMenu = new ConfirmationMenu(title, commandPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
            projectManager.leaveProject(proyectoFinalFinal.getId(), commandPlayer.getUuid());
            String notification;
            if (isLider) notification = LanguageHandler.replaceMC("project.leader.leave.success", language, proyectoFinalFinal);
            else notification = LanguageHandler.replaceMC("project.member.leave.success", language, proyectoFinalFinal  );
            PlayerLogger.info(commandPlayer, notification, (String) null);
        }, cancelClick -> {
            cancelClick.getWhoClicked().closeInventory();
        });
        confirmationMenu.open();
        return true;
    }

}
