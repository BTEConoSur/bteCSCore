package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
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

    private ConfirmationMenu confirmationMenu;
    private ProjectListMenu projectListMenu;

    public ProjectEditCommand() {
        super("edit", "Activar edición de un proyecto existente.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
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
        if (!LinkService.isPlayerLinked(commandPlayer)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "link.mc-link-recomendation"), (String) null);
        }

        ProyectoRegistry pr = ProyectoRegistry.getInstance(); 
        int activeProjects = pr.getCounts(commandPlayer)[1];
        int maxActiveProjects = commandPlayer.getTipoUsuario().getCantProyecSim();
        if (activeProjects >= maxActiveProjects) {
            String message = LanguageHandler.getText(language, "project.leader.max-active-projects").replace("%maxProyectos%", String.valueOf(maxActiveProjects)).replace("%currentProyectos%", String.valueOf(activeProjects));
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        Proyecto targetProyecto = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            targetProyecto = pr.get(proyectoId);
            if (targetProyecto == null) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
                return true;
            }
            if (!permissionManager.isLider(commandPlayer, targetProyecto)) {
                PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.leader.not-leader", language, targetProyecto), (String) null);   
                return true;
            }
        } else {
            Location location = bukkitPlayer.getLocation();
            Set<Proyecto> proyectos = pr.getByLocation(location.getBlockX(), location.getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> liderProyectos = pr.getByLider(commandPlayer, proyectos);
            if (liderProyectos.isEmpty()) {
                    PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.leader.not-leader-here"), (String) null);
                    return true;
            }
            Set<Proyecto> activeProyectos = pr.getCompleted(liderProyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-completed-here"), (String) null);
                return true;
            }
            if (activeProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-completados-list"), activeProyectos, (proyecto, event) -> {
                    if (!permissionManager.isLider(commandPlayer, proyecto)) {
                        PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.leader.not-leader", language, proyecto), (String) null);   
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (proyecto.getEstado() != Estado.COMPLETADO) {
                        PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.not-completed", language, proyecto), (String) null);   
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    String title = LanguageHandler.replaceMC("gui-titles.edit-project-confirm", language, proyecto);
                    confirmationMenu = new ConfirmationMenu(title, bukkitPlayer, projectListMenu, confirmClick -> {
                            confirmClick.getWhoClicked().closeInventory();
                            ProjectManager.getInstance().activateEdit(proyecto.getId(), commandPlayer.getUuid());
                            PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.edit.activate.success", language, commandPlayer, proyecto), (String) null);
                        });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            targetProyecto = activeProyectos.iterator().next();
        }
        if (targetProyecto.getEstado() != Estado.COMPLETADO) {
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.not-completed", language, targetProyecto), (String) null);   
            return true;
        }

        final Proyecto proyectoFinal = targetProyecto;
        confirmationMenu = new ConfirmationMenu(LanguageHandler.replaceMC("gui-titles.edit-project-confirm", language, targetProyecto), bukkitPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
            ProjectManager.getInstance().activateEdit(proyectoFinal.getId(), commandPlayer.getUuid());
            PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.edit.activate.success", language, commandPlayer, proyectoFinal), (String) null);
        }, (cancelClick -> {    
            cancelClick.getWhoClicked().closeInventory();
        }));
        confirmationMenu.open();
        return true;
    }

}
