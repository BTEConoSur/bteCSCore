package com.bteconosur.core.command.reviewer.review;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
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

public class ReviewAcceptCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ProjectFinishReviewMenu confirmationMenu;
    private ProjectListMenu projectListMenu;

    public ReviewAcceptCommand() {
        super("accept", "Aceptar la finalización de un proyecto.", "[comentario]", CommandMode.PLAYER_ONLY);
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
        TipoUsuario postulante = TipoUsuarioRegistry.getInstance().getPostulante();
        ProjectManager pm = ProjectManager.getInstance();
        
        final String comentarioFinal = comentario;
        if (finishingProyectos.size() > 1) {
            projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-activos-list"), finishingProyectos, (proyecto, event) -> {
                String proyectoIdFinal = proyecto.getId();
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
                confirmationMenu = new ProjectFinishReviewMenu(commandPlayer, proyectoIdFinal, lang.getString("gui-titles.finish-project-accept").replace("%proyectoId%", proyectoIdFinal), comentarioFinal, hasPostulantes, projectListMenu);
                confirmationMenu.open();
            });
            projectListMenu.open();
            return true;
        }
        Proyecto proyecto = finishingProyectos.iterator().next();
        String proyectoId = proyecto.getId();
        Player lider = pm.getLider(proyecto);

        confirmationMenu = new ProjectFinishReviewMenu(commandPlayer, proyectoId, lang.getString("gui-titles.finish-project-accept").replace("%proyectoId%", proyectoId), comentarioFinal, postulante.equals(lider.getTipoUsuario()));
        confirmationMenu.open();

        return true;
    }

}
