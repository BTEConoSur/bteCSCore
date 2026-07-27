package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.api.json.api.FinishRequest;
import com.bteconosur.core.api.json.api.PaginaDTO;
import com.bteconosur.core.api.json.api.PlayerSummaryDTO;
import com.bteconosur.core.api.json.api.ProyectoDetailDTO;
import com.bteconosur.core.api.json.api.ProyectoMapaDTO;
import com.bteconosur.core.api.json.api.ProyectoSummaryDTO;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;

public class ProyectoController {

    ProyectoRegistry pr = ProyectoRegistry.getInstance();
    ProjectManager pm = ProjectManager.getInstance();
    InteractionRegistry ir = InteractionRegistry.getInstance();

    public void registrar(RoutesConfig config) {
        config.apiBuilder(() -> {
            path("/api/proyecto", () -> {
                get(this::listar);
                path("/finalizaciones", () -> {
                    get(this::listarFinalizaciones);
                });
                path("/mapa", () -> {
                    get(this::listarParaMapa);
                });
                path("/{id}", () -> {
                    get(this::obtener);
                    put(this::actualizar);
                    delete(this::eliminar);
                    path("/miembros", () -> {
                        get(this::listarMiembros);
                    });
                    path("/solicitudes", () -> {
                        post(this::enviarSolicitud);
                        get(this::obtenerSolicitudes);
                        path("/{playerId}", () -> {
                            post(this::aceptarSolicitud);
                            delete(this::rechazarSolicitud);
                        });
                    });
                    path("/finalizar", () -> {
                        post(this::finalizarProyecto);
                    });
                    path("/aprobar", () -> {
                        post(this::aprobarProyecto);
                    });
                    path("/rechazar", () -> {
                        post(this::rechazarProyecto);
                    });
                    
                });
                
            });
        });
    }

    private void listarParaMapa(Context ctx) {
        List<Proyecto> conPoligono = pr.getList().stream()
            .filter(p -> p.getPoligono() != null)
            .toList();

        List<ProyectoMapaDTO> dtos = conPoligono.stream()
            .map(ProyectoMapaDTO::new)
            .toList();

        ctx.json(dtos);
    }

    private void listar(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = Math.min(ctx.queryParamAsClass("size", Integer.class).getOrDefault(20), 20);
        String idFiltro = ctx.queryParam("id");

        List<Proyecto> todos = new ArrayList<>(pr.getList());

        todos.sort(
            Comparator.comparing(
                Proyecto::getFechaCreado,
                Comparator.nullsLast(Comparator.reverseOrder())
            )
        );

        if (idFiltro != null && !idFiltro.isBlank()) {
            String filtroUpper = idFiltro.toUpperCase();
            todos = todos.stream()
                .filter(p -> p.getId() != null && p.getId().toUpperCase().contains(filtroUpper))
                .toList();
        }

        int total = todos.size();
        int desde = Math.min(page * size, total);
        int hasta = Math.min(desde + size, total);

        List<ProyectoSummaryDTO> pagina = todos.subList(desde, hasta).stream().map(ProyectoSummaryDTO::new).toList();

        ctx.json(new PaginaDTO<ProyectoSummaryDTO>(pagina, page, size, total));
    }

    private void obtener(Context ctx) {
        Proyecto proyecto = pr.get(ctx.pathParam("id"));
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        ctx.json(new ProyectoDetailDTO(proyecto));
    }

    private void actualizar(Context ctx) {
        ProyectoDetailDTO obj = ctx.bodyAsClass(ProyectoDetailDTO.class);
        Proyecto existing = pr.get(ctx.pathParam("id"));
        if (existing == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        existing.setNombre(obj.getNombre());
        existing.setDescripcion(obj.getDescripcion());
        
        ctx.json(new ProyectoDetailDTO(pr.merge(ctx.pathParam("id"))));
    }

    private void eliminar(Context ctx) {
        Proyecto obj = pr.get(ctx.pathParam("id"));
        if (obj == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        pm.deleteProject(obj, null);
        ctx.status(204).result("Proyecto deleted");
    }

    private void listarMiembros(Context ctx) {
        Proyecto proyecto = pr.get(ctx.pathParam("id"));
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        List<PlayerSummaryDTO> miembrosDTO = pm.getMembers(proyecto).stream()
            .map(PlayerSummaryDTO::new)
            .collect(Collectors.toList());
        ctx.json(miembrosDTO);
    }

    private void enviarSolicitud(Context ctx) {
        String proyectoId = ctx.pathParam("id");
        Proyecto proyecto = pr.get(proyectoId);
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        UUID playerId = UUID.fromString(ctx.queryParam("playerId"));
        Player player = PlayerRegistry.getInstance().get(playerId);
        if (player == null) {
            ctx.status(404).result("Player not found");
            return;
        }
        Interaction interaction = ir.findJoinRequest(proyectoId, playerId);
        if (interaction != null) {
            ctx.status(400).result("Join request already exists");
            return;
        }
        
        pm.createJoinRequest(proyectoId, playerId);
        ctx.status(200).result("Join request sent");
    }

    private void obtenerSolicitudes(Context ctx) {
        String proyectoId = ctx.pathParam("id");
        Proyecto proyecto = pr.get(proyectoId);
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        List<PlayerSummaryDTO> solicitudesDTO = pm.getJoinRequests(proyecto).stream()
            .map(PlayerSummaryDTO::new)
            .collect(Collectors.toList());
        ctx.json(solicitudesDTO);
    }

    private void aceptarSolicitud(Context ctx) {
        String proyectoId = ctx.pathParam("id");
        Proyecto proyecto = pr.get(proyectoId);
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        UUID playerId = UUID.fromString(ctx.pathParam("playerId"));
        Player player = PlayerRegistry.getInstance().get(playerId);
        if (player == null) {
            ctx.status(404).result("Player not found");
            return;
        }
        Interaction interaction = ir.findJoinRequest(proyectoId, playerId);
        if (interaction == null) {
            ctx.status(404).result("Join request not found");
            return;
        }
        
        pm.acceptJoinRequest(proyectoId, playerId, proyecto.getLider().getUuid());
        ctx.status(200).result("Join request accepted");
    }

    private void rechazarSolicitud(Context ctx) {
        String proyectoId = ctx.pathParam("id");
        Proyecto proyecto = pr.get(proyectoId);
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        UUID playerId = UUID.fromString(ctx.pathParam("playerId"));
        Player player = PlayerRegistry.getInstance().get(playerId);
        if (player == null) {
            ctx.status(404).result("Player not found");
            return;
        }
        Interaction interaction = ir.findJoinRequest(proyectoId, playerId);
        if (interaction == null) {
            ctx.status(404).result("Join request not found");
            return;
        }

        pm.rejectJoinRequest(proyectoId, playerId, proyecto.getLider().getUuid());
        ctx.status(200).result("Join request rejected");
    }

    private void finalizarProyecto(Context ctx) {
        String proyectoId = ctx.pathParam("id");
        Proyecto proyecto = pr.get(proyectoId);
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        Interaction interaction = ir.findFinishRequest(proyectoId);
        if (interaction != null) {
            ctx.status(404).result("Finish request already exists");
            return;
        }

        pm.createFinishRequest(proyectoId, proyecto.getLider().getUuid());
        ctx.status(200).result("Proyecto marked as finished");
    }

    private void aprobarProyecto(Context ctx) {
        String proyectoId = ctx.pathParam("id");
        Proyecto proyecto = pr.get(proyectoId);
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        FinishRequest finishRequest = ctx.bodyAsClass(FinishRequest.class);
        Player staff = PlayerRegistry.getInstance().get(finishRequest.getStaffId());
        if (staff == null) {
            ctx.status(404).result("Staff player not found");
            return;
        }
        Interaction interaction = ir.findFinishRequest(proyectoId);
        if (interaction == null) {
            ctx.status(404).result("Finish request not found");
            return;
        }

        pm.acceptFinishRequest(proyectoId, staff, finishRequest.getComentario(), finishRequest.getPromote());
        ctx.status(200).result("Proyecto finish request approved");
    }

    private void rechazarProyecto(Context ctx) {
        String proyectoId = ctx.pathParam("id");
        Proyecto proyecto = pr.get(proyectoId);
        if (proyecto == null) {
            ctx.status(404).result("Proyecto not found");
            return;
        }
        FinishRequest finishRequest = ctx.bodyAsClass(FinishRequest.class);
        Player staff = PlayerRegistry.getInstance().get(finishRequest.getStaffId());
        if (staff == null) {
            ctx.status(404).result("Staff player not found");
            return;
        }
        Interaction interaction = ir.findFinishRequest(proyectoId);
        if (interaction == null) {
            ctx.status(404).result("Finish request not found");
            return;
        }

        pm.rejectFinishRequest(proyectoId, staff, finishRequest.getComentario());
        ctx.status(200).result("Proyecto finish request rejected");
    }

    private void listarFinalizaciones(Context ctx) {
        UUID playerId = UUID.fromString(ctx.queryParam("staffId"));
        Player player = PlayerRegistry.getInstance().get(playerId);

        List<Proyecto> todos = pr.getFinishing().stream().collect(Collectors.toList());;
        PermissionManager permissionManager = PermissionManager.getInstance();
		todos.removeIf(proyecto -> !permissionManager.isReviewer(player, proyecto.getPais()));

        List<ProyectoSummaryDTO> result = todos.stream().map(ProyectoSummaryDTO::new).toList();

        ctx.json(result);
    }

}
