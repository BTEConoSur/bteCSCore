package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.menu.project.ProjectManageMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectManageCommand extends BaseCommand {

    private ProjectListMenu projectListMenu;
    private ProjectManageMenu projectManageMenu;

    public ProjectManageCommand() {
        super("manage", "Gestionar un proyecto.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length > 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        Proyecto proyectoFinal = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-found-id", language, proyectoFinal), (String) null);   
                return true;
            }
            if (!permissionManager.isMiembroOrLider(commandPlayer, proyectoFinal)) {
                String message = LanguageHandler.replaceMC("project.member.not-a-member", language, proyectoFinal);   
                PlayerLogger.error(commandPlayer, message, (String) null);
                return true;
            }
        } else {
            Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> liderProyectos = ProyectoRegistry.getInstance().getMemberOrLider(commandPlayer, proyectos);
            if (liderProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.member.not-member-here"), (String) null);
                return true;
            }
            
            if (liderProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-here-list"), liderProyectos, (proyecto, event) -> {
                    projectManageMenu = new ProjectManageMenu(commandPlayer, proyecto, LanguageHandler.replaceMC("gui-titles.project-manage", language, proyecto));
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = liderProyectos.iterator().next();
        }


        projectManageMenu = new ProjectManageMenu(commandPlayer, proyectoFinal, LanguageHandler.replaceMC("gui-titles.project-manage", language, proyectoFinal));
        projectManageMenu.open();
        return true;
    }

}
