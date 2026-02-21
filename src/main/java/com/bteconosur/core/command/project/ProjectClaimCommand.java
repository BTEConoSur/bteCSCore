package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ProjectClaimCommand extends BaseCommand {

    private ProjectListMenu projectListMenu;

    public ProjectClaimCommand() {
        super("claim", "Reclamar un proyecto Abandonado.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
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
        ProjectManager projectManager = ProjectManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyectoFinal = null;
        int maxActiveProjects = commandPlayer.getTipoUsuario().getCantProyecSim();
        int activeProjects = pr.getCounts(commandPlayer)[1];
        if (activeProjects >= maxActiveProjects) {
        String message = LanguageHandler.getText(language, "project.leader.max-active-projects").replace("%maxProyectos%", String.valueOf(maxActiveProjects)).replace("%currentProyectos%", String.valueOf(activeProjects));
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = pr.getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> abandonedProyectos = pr.getAbandoned(proyectos);
            if (abandonedProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-abandoned-here"), (String) null);
                return true;
            }

            if (abandonedProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-abandonados-list"), abandonedProyectos, (proyecto, event) -> {
                    String title = LanguageHandler.replaceMC("gui-titles.claim-project-confirm", language, proyecto);
                    ConfirmationMenu confirmationMenu = new ConfirmationMenu(title, commandPlayer, projectListMenu, confirmClick -> {
                        event.getWhoClicked().closeInventory();
                        if (proyecto.getEstado() != Estado.ABANDONADO) {
                            String message =  LanguageHandler.replaceMC("project.not-abandoned", language, proyecto);
                            PlayerLogger.error(commandPlayer, message, (String) null);
                            return;
                        }
                        projectManager.claim(proyecto.getId(), commandPlayer.getUuid());
                        PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.claim.success", language, proyecto), (String) null);
                    });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = abandonedProyectos.iterator().next();
        }

        final Proyecto proyecto = proyectoFinal;
        String title = LanguageHandler.replaceMC("gui-titles.claim-project-confirm", language, proyectoFinal);
        ConfirmationMenu confirmationMenu = new ConfirmationMenu(title, commandPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
                if (proyecto.getEstado() != Estado.ABANDONADO) {
                String message = LanguageHandler.replaceMC("project.not-abandoned", language, proyecto);
                PlayerLogger.error(commandPlayer, message, (String) null);
                return;
            }
            projectManager.claim(proyecto.getId(), commandPlayer.getUuid());
            PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.claim.success", language, proyecto), (String) null);
        }, cancelClick -> {
            cancelClick.getWhoClicked().closeInventory();
        });
        confirmationMenu.open();
        return true;
    }

}
