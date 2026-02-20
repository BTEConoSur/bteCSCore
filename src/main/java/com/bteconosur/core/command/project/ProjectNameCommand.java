package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectNameCommand extends BaseCommand {

    public ProjectNameCommand() {
        super("name", "Cambiar el nombre de un proyecto.", "<id_proyecto> <nuevo_nombre>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        PermissionManager permissionManager = PermissionManager.getInstance();
        if (args.length != 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(commandPlayer, message, (String) null);
            return true;
        }

        ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
        String proyectoId = args[0];
        Proyecto targetProyecto = proyectoRegistry.get(proyectoId);
        if (targetProyecto == null) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-found-id", language, targetProyecto), (String) null);   
            return true;
        }
        if (!permissionManager.isLider(commandPlayer, targetProyecto)) {
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.leader.not-leader", language, targetProyecto), (String) null);   
            return true;
        }

        String nombre = args[1];
        if (nombre.length() > 50) {
            PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "invalid-project-name"), (String) null);
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        targetProyecto.setNombre(nombre);
        proyectoRegistry.merge(targetProyecto.getId());
        PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.update.name.success", language, commandPlayer, targetProyecto), (String) null);
        Set<Player> miembros = projectManager.getMembers(targetProyecto);
        for (Player miembro : miembros) 
            PlayerLogger.info(miembro, LanguageHandler.replaceMC("project.update.name.for-member", miembro.getLanguage(), targetProyecto), 
                ChatUtil.getDsProjectNameUpdated(targetProyecto, nombre, miembro.getLanguage()));
        String countryLog = LanguageHandler.replaceDS("project.update.name.log", language, commandPlayer, targetProyecto);   
        DiscordLogger.countryLog(countryLog, targetProyecto.getPais());
        return true;
    }
    
}
