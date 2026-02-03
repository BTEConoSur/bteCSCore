package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
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

public class ProjectFinishCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;
    private ProjectListMenu projectListMenu;

    public ProjectFinishCommand() {
        super("finish", "Finalizar un proyecto.", "[id_proyecto]", "btecs.command.project.finish", CommandMode.PLAYER_ONLY);
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Proyecto proyectoFinal = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(bukkitPlayer, lang.getString("no.project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
            if (!permissionManager.isLider(commandPlayer, proyectoFinal)) {
                PlayerLogger.warn(bukkitPlayer, lang.getString("not-a-leader-project").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(bukkitPlayer, lang.getString("no-project-found-here"), (String) null);
                return true;
            }
            if (!permissionManager.isLider(commandPlayer, proyectos)) {
                    PlayerLogger.warn(bukkitPlayer, lang.getString("not-a-leader-here"), (String) null);
                    return true;
            }
            if (proyectos.size() > 1) {
                Set<Proyecto> liderProyectos = ProyectoRegistry.getInstance().getByLiderAndActive(commandPlayer, proyectos);
                if (liderProyectos.isEmpty()) {
                    PlayerLogger.warn(bukkitPlayer, lang.getString("not-a-leader-active-here"), (String) null);
                    return true;
                }
                projectListMenu = new ProjectListMenu(bukkitPlayer, lang.getString("gui-titles.proyectos-activos-list"), proyectos, (proyecto, event) -> {
                    String proyectoIdFinal = proyecto.getId();
                    confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.finish-project-confirm").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, projectListMenu, confirmClick -> {
                            ProjectManager.getInstance().createFinishRequest(proyectoIdFinal);
                            PlayerLogger.info(bukkitPlayer, lang.getString("project-finish-request-success").replace("%proyectoId%", proyectoIdFinal), (String) null);
                            confirmationMenu.getGui().close(bukkitPlayer);
                        });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = proyectos.iterator().next();
        }
        if (proyectoFinal.getEstado() != Estado.ACTIVO) {
            PlayerLogger.warn(bukkitPlayer, lang.getString("not-a-active-project").replace("%proyectoId%", proyectoFinal.getId()), (String) null);   
            return true;
        }

        final String proyectoIdFinal = proyectoFinal.getId();
        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.finish-project-confirm").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, confirmClick -> {
                ProjectManager.getInstance().createFinishRequest(proyectoIdFinal);
                PlayerLogger.info(bukkitPlayer, lang.getString("project-finish-request-success").replace("%proyectoId%", proyectoIdFinal), (String) null);
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {    
                confirmationMenu.getGui().close(bukkitPlayer);
        }));
        confirmationMenu.open();
        return true;
    }

}
