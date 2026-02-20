package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.project.JoinRequestListMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectAcceptCommand extends BaseCommand {

    private ProjectListMenu projectListMenu;

    public ProjectAcceptCommand() {
        super("accept", "Aceptar o rechazar solicitudes de unión.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length > 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
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
            if (!permissionManager.isLider(commandPlayer, proyectoFinal)) {
                PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.lider.not-lider", language, proyectoFinal), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> activeProyectos = ProyectoRegistry.getInstance().getActiveOrEditando(proyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-active-editing"), (String) null); //TODO: verificar casos en editando
                return true;
            }
            Set<Proyecto> liderProyectos = ProyectoRegistry.getInstance().getByLider(commandPlayer, activeProyectos);
            if (liderProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.leader.not-leader-active-editing-here"), (String) null);
                return true;
            }

            if (liderProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-here-list"), liderProyectos, (proyecto, event) -> {
                    String title = LanguageHandler.replaceMC("gui-titles.join-request-list", language, proyecto);
                    new JoinRequestListMenu(commandPlayer, title, proyecto, projectListMenu).open();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = liderProyectos.iterator().next();
        }

        String title = LanguageHandler.replaceMC("gui-titles.join-request-list", language, proyectoFinal);
        new JoinRequestListMenu(commandPlayer, title, proyectoFinal).open();

        return true;
    }

}

