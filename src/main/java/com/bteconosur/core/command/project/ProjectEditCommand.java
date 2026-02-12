package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.discord.util.LinkService;

public class ProjectEditCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;
    private ProjectListMenu projectListMenu;

    public ProjectEditCommand() {
        super("edit", "Activar edición de un proyecto existente.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        if (!LinkService.isPlayerLinked(commandPlayer)) {
            PlayerLogger.warn(commandPlayer, lang.getString("minecraft-link-recomendation"), (String) null);
        }

        ProyectoRegistry pr = ProyectoRegistry.getInstance(); 
        int activeProjects = pr.getCounts(commandPlayer)[1];
        int maxActiveProjects = commandPlayer.getTipoUsuario().getCantProyecSim();
        if (activeProjects >= maxActiveProjects) {
            String message = lang.getString("max-active-projects").replace("%maxProjects%", String.valueOf(maxActiveProjects)).replace("%currentProjects%", String.valueOf(activeProjects));
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        Proyecto targetProyecto = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            targetProyecto = pr.get(proyectoId);
            if (targetProyecto == null) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
            if (!permissionManager.isLider(commandPlayer, targetProyecto)) {
                PlayerLogger.error(commandPlayer, lang.getString("not-a-leader-project").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
        } else {
            Location location = bukkitPlayer.getLocation();
            Set<Proyecto> proyectos = pr.getByLocation(location.getBlockX(), location.getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> liderProyectos = pr.getByLider(commandPlayer, proyectos);
            if (liderProyectos.isEmpty()) {
                    PlayerLogger.warn(commandPlayer, lang.getString("not-a-leader-here"), (String) null);
                    return true;
            }
            Set<Proyecto> activeProyectos = pr.getCompleted(liderProyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-completed-here"), (String) null);
                return true;
            }
            if (activeProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-completados-list"), activeProyectos, (proyecto, event) -> {
                    String proyectoIdFinal = proyecto.getId();
                    if (!permissionManager.isLider(commandPlayer, proyecto)) {
                        PlayerLogger.error(commandPlayer, lang.getString("not-a-leader-project").replace("%proyectoId%", proyectoIdFinal), (String) null);   
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (proyecto.getEstado() != Estado.COMPLETADO) {
                        PlayerLogger.error(commandPlayer, lang.getString("not-a-completed-project").replace("%proyectoId%", proyecto.getId()), (String) null);   
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.edit-project-confirm").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, projectListMenu, confirmClick -> {
                            confirmClick.getWhoClicked().closeInventory();
                            ProjectManager.getInstance().activateEdit(proyecto.getId(), commandPlayer.getUuid());
                            PlayerLogger.info(bukkitPlayer, lang.getString("project-edit-success").replace("%proyectoId%", proyecto.getId()), (String) null);
                        });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            targetProyecto = activeProyectos.iterator().next();
        }
        if (targetProyecto.getEstado() != Estado.COMPLETADO) {
            PlayerLogger.error(commandPlayer, lang.getString("not-a-completed-project").replace("%proyectoId%", targetProyecto.getId()), (String) null);   
            return true;
        }

        final String proyectoIdFinal = targetProyecto.getId();
        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.edit-project-confirm").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
            ProjectManager.getInstance().activateEdit(proyectoIdFinal, commandPlayer.getUuid());
            PlayerLogger.info(bukkitPlayer, lang.getString("project-edit-success").replace("%proyectoId%", proyectoIdFinal), (String) null);
        }, (cancelClick -> {    
            cancelClick.getWhoClicked().closeInventory();
        }));
        confirmationMenu.open();
        return true;
    }

}
