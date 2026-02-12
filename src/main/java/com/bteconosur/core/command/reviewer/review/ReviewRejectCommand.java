package com.bteconosur.core.command.reviewer.review;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ReviewRejectCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;
    private ConfirmationMenu confirmationEditMenu;
    private ProjectListMenu projectListMenu;

    public ReviewRejectCommand() {
        super("reject", "Rechazar la finalización de un proyecto.", "[comentario]", CommandMode.PLAYER_ONLY);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        String comentario = null;

        if (args.length >= 1) {
            StringBuilder comentarioBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) comentarioBuilder.append(" ");
                comentarioBuilder.append(args[i]);
            }
            comentario = comentarioBuilder.toString();

            if (comentario.length() > 300) {
                PlayerLogger.error(commandPlayer, lang.getString("invalid-project-comment"), (String) null);
                return true;
            }
        }

        Location location = bukkitPlayer.getLocation();
        Pais pais = PaisRegistry.getInstance().findByLocation(location.getBlockX(), location.getBlockZ());  // Capaz que es mejor usar del proyecto;
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-reviewer-country").replace("%pais%", pais.getNombrePublico()), (String) null);
            return true;
        }
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Set<Proyecto> proyectos = pr.getByLocation(location.getBlockX(), location.getBlockZ());
        if (proyectos.isEmpty()) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-here"), (String) null);
            return true;
        }
        
        Set<Proyecto> finishingProyectos = pr.getFinishing(proyectos);
        if (finishingProyectos.isEmpty()) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-finishing-here"), (String) null);
            return true;
        }
        ProjectManager pm = ProjectManager.getInstance();

        final String finalComentario = comentario;
        if (finishingProyectos.size() > 1) {
            projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-activos-list"), finishingProyectos, (proyecto, event) -> {
                String proyectoIdFinal = proyecto.getId();
                if (proyecto.getEstado() == Estado.EN_FINALIZACION_EDICION) {
                    confirmationEditMenu = new ConfirmationMenu(lang.getString("gui-titles.finish-edit-project-reject").replace("%proyectoId%", proyecto.getId()), bukkitPlayer, confirmClick -> {
                            confirmClick.getWhoClicked().closeInventory();
                            ProjectManager.getInstance().rejectedEditRequest(proyecto.getId(), commandPlayer, finalComentario);
                            PlayerLogger.info(bukkitPlayer, lang.getString("project-finish-edit-staff-rejected").replace("%proyectoId%", proyecto.getId()), (String) null);
                        }, (cancelClick -> {    
                            cancelClick.getWhoClicked().closeInventory();
                    }));
                    confirmationEditMenu.open();
                    return;
                }
                confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.finish-project-reject").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, projectListMenu, confirmClick -> {
                    pm.rejectFinishRequest(proyectoIdFinal, commandPlayer, finalComentario);
                    PlayerLogger.info(bukkitPlayer, lang.getString("project-finish-staff-rejected").replace("%proyectoId%", proyectoIdFinal), (String) null);
                    confirmationMenu.getGui().close(bukkitPlayer);
                });
                confirmationMenu.open();
            });
            projectListMenu.open();
            PlayerLogger.warn(bukkitPlayer, "Se han encontrado múltiples proyectos aquí. Por favor, especifica el ID del proyecto.", (String) null);
            return true;
        }
        Proyecto proyecto = finishingProyectos.iterator().next();
        if (proyecto.getEstado() == Estado.EN_FINALIZACION_EDICION) {
            confirmationEditMenu = new ConfirmationMenu(lang.getString("gui-titles.finish-edit-project-reject").replace("%proyectoId%", proyecto.getId()), bukkitPlayer, confirmClick -> {
                    confirmClick.getWhoClicked().closeInventory();
                    ProjectManager.getInstance().rejectedEditRequest(proyecto.getId(), commandPlayer, finalComentario);
                    PlayerLogger.info(bukkitPlayer, lang.getString("project-finish-edit-staff-rejected").replace("%proyectoId%", proyecto.getId()), (String) null);
                }, (cancelClick -> {    
                    cancelClick.getWhoClicked().closeInventory();
            }));
            confirmationEditMenu.open();
            return true;
        }
        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.finish-project-reject").replace("%proyectoId%", proyecto.getId()), bukkitPlayer, confirmClick -> {
                pm.rejectFinishRequest(proyecto.getId(), commandPlayer, finalComentario);
                PlayerLogger.info(bukkitPlayer, lang.getString("project-finish-staff-rejected").replace("%proyectoId%", proyecto.getId()), (String) null);
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {    
                confirmationMenu.getGui().close(bukkitPlayer);
        }));
        confirmationMenu.open();

        return true;
    }

}
