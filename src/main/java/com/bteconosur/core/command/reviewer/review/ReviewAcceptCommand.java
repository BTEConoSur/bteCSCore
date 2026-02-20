package com.bteconosur.core.command.reviewer.review;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectFinishReviewMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;
import com.bteconosur.db.util.Estado;

public class ReviewAcceptCommand extends BaseCommand {

    private ProjectFinishReviewMenu confirmationMenu;
    private ConfirmationMenu confirmationEditMenu;
    private ProjectListMenu projectListMenu;

    public ReviewAcceptCommand() {
        super("accept", "Aceptar la finalización de un proyecto.", "[comentario]", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
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
                PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "invalid-project-comment"), (String) null);
                return true;
            }
        }

        Location location = bukkitPlayer.getLocation();
        Pais pais = PaisRegistry.getInstance().findByLocation(location.getBlockX(), location.getBlockZ());
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("reviewer.not-reviewer-country", language, pais), (String) null);
            return true;
        }

        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Set<Proyecto> proyectos = pr.getByLocation(location.getBlockX(), location.getBlockZ());
        if (proyectos.isEmpty()) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
            return true;
        }
        
        Set<Proyecto> finishingProyectos = pr.getFinishing(proyectos);
        if (finishingProyectos.isEmpty()) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-finishing-here"), (String) null);
            return true;
        }
        TipoUsuario postulante = TipoUsuarioRegistry.getInstance().getPostulante();
        ProjectManager pm = ProjectManager.getInstance();
        
        final String comentarioFinal = comentario;
        if (finishingProyectos.size() > 1) {
            projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-activos-list"), finishingProyectos, (proyecto, event) -> {
                String proyectoIdFinal = proyecto.getId();
                if (proyecto.getEstado() == Estado.EN_FINALIZACION_EDICION) {
                    String title = LanguageHandler.replaceMC("gui-titles.finish-edit-project-accept", language, proyecto);
                    confirmationEditMenu = new ConfirmationMenu(title, bukkitPlayer, projectListMenu, confirmClick -> {
                            confirmClick.getWhoClicked().closeInventory();
                            ProjectManager.getInstance().acceptEditRequest(proyectoIdFinal, commandPlayer, comentarioFinal);
                            PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.edit.finish.accept.success", language, proyecto), (String) null);
                        });
                    confirmationEditMenu.open();
                    return;
                }
                Boolean hasPostulantes = false;
                Player lider = pm.getLider(proyecto);
                Set<Player> miembros = pm.getMembers(proyecto);
                if (postulante.equals(lider.getTipoUsuario())) {
                    hasPostulantes = true;
                } else {
                    for (Player miembro : miembros) {
                        if (postulante.equals(miembro.getTipoUsuario())) {
                            hasPostulantes = true;
                            break;
                        }
                    }
                }
                confirmationMenu = new ProjectFinishReviewMenu(commandPlayer, proyecto, LanguageHandler.replaceMC("gui-titles.finish-project-accept", language, proyecto), comentarioFinal, hasPostulantes, projectListMenu);
                confirmationMenu.open();
            });
            projectListMenu.open();
            return true;
        }
        Proyecto proyecto = finishingProyectos.iterator().next();
        String proyectoId = proyecto.getId();
        Player lider = pm.getLider(proyecto);

        if (proyecto.getEstado() == Estado.EN_FINALIZACION_EDICION) {
            String title = LanguageHandler.replaceMC("gui-titles.finish-edit-project-accept", language, proyecto);
            confirmationEditMenu = new ConfirmationMenu(title, bukkitPlayer, confirmClick -> {
                    confirmClick.getWhoClicked().closeInventory();
                    ProjectManager.getInstance().acceptEditRequest(proyectoId, commandPlayer, comentarioFinal);
                    PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.edit.finish.accept.success", language, proyecto), (String) null);
                }, (cancelClick -> {    
                    cancelClick.getWhoClicked().closeInventory();
            }));
            confirmationEditMenu.open();
            return true;
        }
        confirmationMenu = new ProjectFinishReviewMenu(commandPlayer, proyecto, LanguageHandler.replaceMC("gui-titles.finish-project-accept", language, proyecto), comentarioFinal, postulante.equals(lider.getTipoUsuario()));
        confirmationMenu.open();

        return true;
    }

}
