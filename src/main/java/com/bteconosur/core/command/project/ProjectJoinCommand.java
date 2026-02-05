package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectJoinCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ProjectListMenu projectListMenu;

    public ProjectJoinCommand() {
        super("join", "Unirse a un proyecto.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProjectManager projectManager = ProjectManager.getInstance();
        Proyecto proyectoFinal = null;

        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
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
            Set<Proyecto> notMemberProyectos = ProyectoRegistry.getInstance().getNotMemberOrLider(activeProyectos);
            if (notMemberProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("project-join-already-member-here"), (String) null);
                return true;
            }

            if (notMemberProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-activos-list"), notMemberProyectos, (proyecto, event) -> {
                    if (permissionManager.isMiembroOrLider(commandPlayer, proyecto)) {
                        String message = lang.getString("project-join-already-member").replace("%proyectoId%", proyecto.getId());   
                        PlayerLogger.warn(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    projectManager.createJoinRequest(proyecto.getId(), commandPlayer.getUuid());
                    event.getWhoClicked().closeInventory();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = notMemberProyectos.iterator().next();
        }

        if (permissionManager.isMiembroOrLider(commandPlayer, proyectoFinal)) {
            String message = lang.getString("project-join-already-member").replace("%proyectoId%", proyectoFinal.getId());   
            PlayerLogger.warn(commandPlayer, message, (String) null);
            return true;
        }

        projectManager.createJoinRequest(proyectoFinal.getId(), commandPlayer.getUuid());
        return true;
    }

}
