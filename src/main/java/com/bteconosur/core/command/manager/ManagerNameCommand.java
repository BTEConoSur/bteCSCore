package com.bteconosur.core.command.manager;

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
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ManagerNameCommand extends BaseCommand {

    public ManagerNameCommand() {
        super("name", "Cambiar el nombre de un proyecto del país.", "<id_proyecto> <nuevo_nombre>", CommandMode.PLAYER_ONLY);
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
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
            return true;
        }
        Pais pais = targetProyecto.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("manager.not-manager-country", language, pais), (String) null);   
            return true;
        }

        String nombre = args[1];
        if (nombre.length() > 50) {
            PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "invalid-project-name"), (String) null);
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        targetProyecto.setNombre(nombre);
        targetProyecto = proyectoRegistry.merge(targetProyecto.getId());
        PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.update.name.success", language, commandPlayer, targetProyecto), (String) null);
        Set<Player> miembros = projectManager.getMembers(targetProyecto);
        miembros.add(projectManager.getLider(targetProyecto));
        for (Player miembro : miembros) 
            PlayerLogger.info(miembro, LanguageHandler.replaceMC("project.update.name.for-member", miembro.getLanguage(), targetProyecto), 
                ChatUtil.getDsProjectNameUpdated(targetProyecto, nombre, miembro.getLanguage()));
        String countryLog = LanguageHandler.replaceDS("project.update.name.log", language, commandPlayer, targetProyecto);   
        DiscordLogger.countryLog(countryLog, pais);
        return true;
    }

}
