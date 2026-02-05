package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.project.JoinRequestListMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectAcceptCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ProjectListMenu projectListMenu;

    public ProjectAcceptCommand() {
        super("accept", "Aceptar o rechazar solicitudes de unión.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        PermissionManager permissionManager = PermissionManager.getInstance();
        Proyecto proyectoFinal = null;

        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
            if (!permissionManager.isLider(commandPlayer, proyectoFinal)) {
                PlayerLogger.warn(commandPlayer, lang.getString("not-a-leader-project").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> activeProyectos = ProyectoRegistry.getInstance().getActiveOrEditando(proyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-active-editing-here"), (String) null); //TODO: verificar casos en editando
                return true;
            }
            Set<Proyecto> liderProyectos = ProyectoRegistry.getInstance().getByLider(commandPlayer, activeProyectos);
            if (liderProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("not-a-leader-project-active-editing"), (String) null);
                return true;
            }

            if (liderProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-here-list"), liderProyectos, (proyecto, event) -> {
                    String title = lang.getString("gui-titles.join-request-list").replace("%proyectoId%", proyecto.getId());
                    new JoinRequestListMenu(commandPlayer, title, proyecto, projectListMenu).open();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = liderProyectos.iterator().next();
        }

        String title = lang.getString("gui-titles.join-request-list").replace("%proyectoId%", proyectoFinal.getId());
        new JoinRequestListMenu(commandPlayer, title, proyectoFinal).open();

        return true;
    }

}

