package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.api.json.api.DivisionSummaryDTO;
import com.bteconosur.core.api.json.api.PlayerDetailDTO;
import com.bteconosur.core.api.json.api.PlayerSummaryDTO;
import com.bteconosur.core.api.json.api.ProyectoSummaryDTO;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;

public class PlayerController {

    PlayerRegistry pr = PlayerRegistry.getInstance();

    public void registrar(RoutesConfig config) {
        config.apiBuilder(() -> {
            path("/api/player", () -> {
                get(this::listar);
                path("/{id}", () -> {
                    get(this::obtener);
                    put(this::actualizar);
                    delete(this::eliminar);
                });
                path("/proyectos", () -> {
                    get(this::listarProyectos);
                });
            });
        });
    }

    private void listar(Context ctx) {
        List<PlayerSummaryDTO> summ = pr.getList().stream()
            .map(PlayerSummaryDTO::new)
            .toList();
        ctx.json(summ);
    }

    private void obtener(Context ctx) {
        Player obj = pr.get(UUID.fromString(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Player not found");
            return;
        }
        ctx.json(new PlayerDetailDTO(obj));
    }

    private void actualizar(Context ctx) {
        PlayerDetailDTO obj = ctx.bodyAsClass(PlayerDetailDTO.class);
        Player existing = pr.get(UUID.fromString(ctx.pathParam("id")));
        if (existing == null) {
            ctx.status(404).result("Player not found");
            return;
        }
        existing.setNombre(obj.getNombre());
        existing.setNombrePublico(obj.getNombrePublico());
        existing.setFechaIngreso(obj.getFechaIngreso());
        existing.setFechaUltimaConexion(obj.getFechaUltimaConexion());
        existing.setDsIdUsuario(obj.getDsIdUsuario());

        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        Pais newPais = paisRegistry.get(obj.getPaisPrefix() != null ? obj.getPaisPrefix().getId() : null);
        if (existing.getPaisPrefix() != null && newPais != null && !existing.getPaisPrefix().equals(newPais)) {
            existing.setPaisPrefix(newPais);
        } else if (existing.getPaisPrefix() == null && obj.getPaisPrefix() != null) {
            existing.setPaisPrefix(null);
        }

        Player updatedPlayer = pr.merge(UUID.fromString(ctx.pathParam("id")));

        PermissionManager pm = PermissionManager.getInstance();
        if (!existing.getRangoUsuario().equals(obj.getRangoUsuario())) {
            updatedPlayer = pm.switchRangoUsuario(updatedPlayer, obj.getRangoUsuario());
        }
        if (!existing.getTipoUsuario().equals(obj.getTipoUsuario())) {
            updatedPlayer = pm.switchTipoUsuario(updatedPlayer, obj.getTipoUsuario());
        }
    
        ctx.json(updatedPlayer);
    }

    private void eliminar(Context ctx) {
        Player obj = pr.get(UUID.fromString(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Player not found");
            return;
        }
        pr.unload(UUID.fromString(ctx.pathParam("id")));
        ctx.status(204).result("Player deleted");
    }

    private void listarProyectos(Context ctx) {
        Player obj = pr.get(UUID.fromString(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Player not found");
            return;
        }
        LinkedHashSet<Proyecto> proyectos = ProyectoRegistry.getInstance().getByPlayer(obj);
        if (proyectos == null) {
            ctx.status(404).result("No projects found for this Player");
            return;
        }
        List<ProyectoSummaryDTO> proyectoDTOs = proyectos.stream()
            .map(ProyectoSummaryDTO::new)
            .toList();
        ctx.json(proyectoDTOs);
    }

}
