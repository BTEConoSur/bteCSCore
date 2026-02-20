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

public class ProjectDescriptionCommand extends BaseCommand {

    public ProjectDescriptionCommand() {
        super("description", "Cambiar la descripción de un proyecto.", "<id_proyecto> <nueva_descripcion>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        PermissionManager permissionManager = PermissionManager.getInstance();
        if (args.length <= 1) {
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

        StringBuilder descripcionBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) descripcionBuilder.append(" ");
            descripcionBuilder.append(args[i]);
        }
        String descripcion = descripcionBuilder.toString();

        if (descripcion.length() > 100) {
            PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "invalid-project-description"), (String) null);
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        targetProyecto.setDescripcion(descripcion);
        proyectoRegistry.merge(targetProyecto.getId());
        PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.update.description.success", language, commandPlayer, targetProyecto), (String) null);
        Set<Player> miembros = projectManager.getMembers(targetProyecto);
        for (Player miembro : miembros) 
            PlayerLogger.info(miembro, LanguageHandler.replaceMC("project.update.description.for-member", miembro.getLanguage(), targetProyecto),
                ChatUtil.getDsProjectDescriptionUpdated(targetProyecto, descripcion, miembro.getLanguage()));
        String countryLog = LanguageHandler.replaceDS("project.update.description.log", language, commandPlayer, targetProyecto);
        DiscordLogger.countryLog(countryLog, targetProyecto.getPais());
        return true;
    }

}
