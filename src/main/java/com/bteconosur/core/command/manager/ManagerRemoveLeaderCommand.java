package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ManagerRemoveLeaderCommand extends BaseCommand {

    public ManagerRemoveLeaderCommand() {
        super("removeleader", "Quitar a un líder de un proyecto del país.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(commandPlayer, message, (String) null);
            return true;
        }

        PermissionManager permissionManager = PermissionManager.getInstance();

        ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
        String proyectoId = args[0];
        Proyecto targetProyecto = proyectoRegistry.get(proyectoId);
        if (targetProyecto == null) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
            return true;
        }

        Pais pais = targetProyecto.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("manager.not-manager-country", language, pais), (String) null);   
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        Player lider = projectManager.getLider(targetProyecto);
        if (permissionManager.hasMembers(targetProyecto)) {
            String message = LanguageHandler.replaceMC("project.leader.leave.cant-leave-staff", language, targetProyecto);
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }
        
        projectManager.removeFromProject(targetProyecto.getId(), lider.getUuid(), commandPlayer.getUuid());
        String successMessage = LanguageHandler.replaceMC("project.leader.remove.staff", language, lider, targetProyecto);
        PlayerLogger.info(commandPlayer, successMessage, (String) null);

        return true;
    }

}
