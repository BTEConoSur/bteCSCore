package com.bteconosur.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

/**
 * Servicio para gestionar los permisos de construcción temporales de los revisores.
 * Permite a los revisores activar/desactivar permisos de construcción en proyectos específicos.
 */
public class ReviewerToggleBuildService {

    private static HashMap<UUID, String> reviewerBuildToggles = new HashMap<>();

    /**
     * Obtiene el ID del proyecto en el que el revisor tiene activado el permiso de construcción.
     * 
     * @param reviewerUuid UUID del revisor
     * @return ID del proyecto, o null si no tiene ningún toggle activo
     */
    public static String getBuildEnabled(UUID reviewerUuid) {
        return reviewerBuildToggles.get(reviewerUuid);
    }

    /**
     * Obtiene todos los revisores que tienen activado el permiso de construcción en un proyecto específico.
     * 
     * @param proyectoId ID del proyecto
     * @return Conjunto de UUIDs de los revisores con toggle activo en el proyecto
     */
    public static Set<UUID> getReviewersForProject(String proyectoId) {
        Set<UUID> reviewers = new HashSet<>();
        for (HashMap.Entry<UUID, String> entry : reviewerBuildToggles.entrySet()) {
            if (entry.getValue().equals(proyectoId)) {
                reviewers.add(entry.getKey());
            }
        }
        return reviewers;
    }

    /**
     * Alterna el permiso de construcción de un revisor en un proyecto específico.
     * Si el revisor ya tenía un toggle activo en otro proyecto, se cambia al nuevo proyecto.
     * Si el toggle era en el mismo proyecto, se desactiva.
     * Se tiene en cuenta los casos en el que el reviewer es miembro del proyecto.
     * 
     * @param player Jugador que activa/desactiva el toggle
     * @param proyecto Proyecto en el que se activa/desactiva el permiso
     */
    public static void toggle(Player player, Proyecto proyecto) {
        WorldManager wm = WorldManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        PermissionManager pm = PermissionManager.getInstance();
        String proyectoId = proyecto.getId();
        UUID reviewerUuid = player.getUuid();
        boolean isMiembroOrLider = pm.isMiembroOrLider(player, proyecto);
        boolean isActiveOrEditing = proyecto.getEstado() == Estado.ACTIVO || proyecto.getEstado() == Estado.EDITANDO;
        
        if (reviewerBuildToggles.containsKey(reviewerUuid)) {
            String toggledProyectoId = reviewerBuildToggles.get(reviewerUuid);
            if (!proyectoId.equals(toggledProyectoId)) {
                Proyecto toggledProyecto = pr.get(toggledProyectoId);
                boolean wasMemberOfPrevious = pm.isMiembroOrLider(player, toggledProyecto);
                boolean prevActiveOrEditing = toggledProyecto.getEstado() == Estado.ACTIVO || toggledProyecto.getEstado() == Estado.EDITANDO;
                if (!wasMemberOfPrevious || !prevActiveOrEditing) {
                    wm.removePlayer(toggledProyecto, reviewerUuid);
                }
                reviewerBuildToggles.put(reviewerUuid, proyectoId);
                if (!isMiembroOrLider || !isActiveOrEditing) {
                    wm.addPlayer(proyecto, reviewerUuid);
                }
                String message = LanguageHandler.replaceMC("reviewer.toggle-build.switched", player.getLanguage(), proyecto);
                PlayerLogger.info(player, message, (String) null);
            } else {
                reviewerBuildToggles.remove(reviewerUuid);
                if (!isMiembroOrLider || !isActiveOrEditing) {
                    wm.removePlayer(proyecto, reviewerUuid);
                }
                String message = LanguageHandler.replaceMC("reviewer.toggle-build.disabled", player.getLanguage(), proyecto);
                PlayerLogger.info(player, message, (String) null);
            }
        } else {
            reviewerBuildToggles.put(reviewerUuid, proyectoId);
            if (!isMiembroOrLider || !isActiveOrEditing) {
                wm.addPlayer(proyecto, reviewerUuid);
            }
            String message = LanguageHandler.replaceMC("reviewer.toggle-build.enabled", player.getLanguage(), proyecto);
            PlayerLogger.info(player, message, (String) null);
        }
    }

}
