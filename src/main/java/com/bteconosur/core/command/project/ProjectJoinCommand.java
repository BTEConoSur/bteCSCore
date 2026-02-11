package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ProjectJoinCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final YamlConfiguration config;
    private ProjectListMenu projectListMenu;

    public ProjectJoinCommand() { // Postulantes no pueden usar este comando.
        super("join", "Unirse a un proyecto Activo o en Edición.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
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
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
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
            Set<Proyecto> roomProyectos = pr.getWithRoom(proyectos);
            if (roomProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-projects-with-room-here"), (String) null);
                return true;
            }
            Set<Proyecto> activeProyectos = pr.getActiveOrEditando(roomProyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-active-editing-here"), (String) null); //TODO: verificar casos en editando
                return true;
            }
            Set<Proyecto> notMemberProyectos = pr.getNotMemberOrLider(commandPlayer, activeProyectos);
            if (notMemberProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("project-join-already-member-here"), (String) null);
                return true;
            }

            if (notMemberProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-activos-list"), notMemberProyectos, (proyecto, event) -> {
                    if (permissionManager.isMiembroOrLider(commandPlayer, proyecto)) {
                        String message = lang.getString("project-join-already-member").replace("%proyectoId%", proyecto.getId());   
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (proyecto.getEstado() != Estado.ACTIVO && proyecto.getEstado() != Estado.EDITANDO) {
                        String message = lang.getString("not-a-active-editing-project").replace("%proyectoId%", proyecto.getId());
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (!proyecto.checkMaxMiembros()) {
                        String message = lang.getString("max-members-reached").replace("%proyectoId%", proyecto.getId())
                            .replace("%max%", String.valueOf(proyecto.getTipoProyecto().getMaxMiembros()))
                            .replace("%current%", String.valueOf(proyecto.getCantMiembros()));
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }

                    Player lider = projectManager.getLider(proyecto);
                    int maxMembers = config.getInt("max-members-for-postulantes");
                    if (permissionManager.isPostulante(lider) && proyecto.getCantMiembros() >= maxMembers) {
                        String message = lang.getString("postulante-cant-add-member").replace("%max%", String.valueOf(maxMembers));
                        PlayerLogger.error(commandPlayer, message, (String) null);
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
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        if (proyectoFinal.getEstado() != Estado.ACTIVO && proyectoFinal.getEstado() != Estado.EDITANDO) {
            String message = lang.getString("not-a-active-editing-project").replace("%proyectoId%", proyectoFinal.getId());
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        if (!proyectoFinal.checkMaxMiembros()) {
            String message = lang.getString("max-members-reached").replace("%proyectoId%", proyectoFinal.getId())
                .replace("%max%", String.valueOf(proyectoFinal.getTipoProyecto().getMaxMiembros()))
                .replace("%current%", String.valueOf(proyectoFinal.getCantMiembros()));
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        Player lider = projectManager.getLider(proyectoFinal);
        int maxMembers = config.getInt("max-members-for-postulantes");
        if (permissionManager.isPostulante(lider) && proyectoFinal.getCantMiembros() >= maxMembers) {
            String message = lang.getString("postulante-cant-add-member").replace("%max%", String.valueOf(maxMembers));
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        projectManager.createJoinRequest(proyectoFinal.getId(), commandPlayer.getUuid());
        return true;
    }

}
