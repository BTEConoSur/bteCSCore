package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ProjectClaimCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ProjectListMenu projectListMenu;

    public ProjectClaimCommand() {
        super("claim", "Reclamar un proyecto Abandonado.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
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
            Set<Proyecto> abandonedProyectos = pr.getAbandoned(proyectos);
            if (abandonedProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-abandoned-here"), (String) null); //TODO: verificar casos en editando
                return true;
            }

            if (abandonedProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-abandonados-list"), abandonedProyectos, (proyecto, event) -> {
                    ConfirmationMenu confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.claim-project-confirm").replace("%proyectoId%", proyecto.getId()), commandPlayer, projectListMenu, confirmClick -> {
                        event.getWhoClicked().closeInventory();
                        if (proyecto.getEstado() != Estado.ABANDONADO) {
                            String message = lang.getString("not-a-abandoned-project").replace("%proyectoId%", proyecto.getId());
                            PlayerLogger.error(commandPlayer, message, (String) null);
                            return;
                        }
                        projectManager.claim(proyecto.getId(), commandPlayer.getUuid());
                        PlayerLogger.info(commandPlayer, lang.getString("project-claimed").replace("%proyectoId%", proyecto.getId()), (String) null);
                    });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = abandonedProyectos.iterator().next();
        }

        final String proyectoIdFinal = proyectoFinal.getId();
        final Estado proyectoEstado = proyectoFinal.getEstado();
        ConfirmationMenu confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.claim-project-confirm").replace("%proyectoId%", proyectoIdFinal), commandPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
                if (proyectoEstado != Estado.ABANDONADO) {
                String message = lang.getString("not-a-abandoned-project").replace("%proyectoId%", proyectoIdFinal);
                PlayerLogger.error(commandPlayer, message, (String) null);
                return;
            }
            projectManager.claim(proyectoIdFinal, commandPlayer.getUuid());
            PlayerLogger.info(commandPlayer, lang.getString("project-claimed").replace("%proyectoId%", proyectoIdFinal), (String) null);
        }, cancelClick -> {
            cancelClick.getWhoClicked().closeInventory();
        });
        confirmationMenu.open();
        return true;
    }

}
