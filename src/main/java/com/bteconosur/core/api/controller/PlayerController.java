package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import com.bteconosur.core.api.json.api.PaginaDTO;
import com.bteconosur.core.api.json.api.PlayerDetailDTO;
import com.bteconosur.core.api.json.api.PlayerSummaryDTO;
import com.bteconosur.core.api.json.api.ProyectoSummaryDTO;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;

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
                    path("/proyectos", () -> {
                        get(this::listarProyectos);
                    });
                });
                path("/discord/{discordId}", () -> {
                    get(this::obtenerPorDiscordId);
                });
            });
        });
    }

    private void obtenerPorDiscordId(Context ctx) {
        Long discordId = Long.valueOf(ctx.pathParam("discordId"));
        Player player = pr.findByDiscordId(discordId);
        if (player == null) {
            ctx.status(404).result("Player not linked to this Discord account");
            return;
        }
        ctx.json(new PlayerDetailDTO(player));
    }

    private void listar(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = Math.min(ctx.queryParamAsClass("size", Integer.class).getOrDefault(20), 20);
        String nombreFiltro = ctx.queryParam("nombre");

        List<Player> todos = pr.getList();

        if (nombreFiltro != null && !nombreFiltro.isBlank()) {
            String filtroLower = nombreFiltro.toLowerCase();
            todos = todos.stream()
                .filter(p -> p.getNombre() != null && p.getNombre().toLowerCase().contains(filtroLower))
                .toList();
        }

        int total = todos.size();
        int desde = Math.min(page * size, total);
        int hasta = Math.min(desde + size, total);

        List<PlayerSummaryDTO> pagina = todos.subList(desde, hasta).stream().map(PlayerSummaryDTO::new).toList();

        ctx.json(new PaginaDTO<PlayerSummaryDTO>(pagina, page, size, total));
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
        //existing.setFechaIngreso(new Date(obj.getFechaIngreso()));
        //existing.setFechaUltimaConexion(new Date(obj.getFechaUltimaConexion()));
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
            RangoUsuario newRango = RangoUsuarioRegistry.getInstance().get(obj.getRangoUsuario().getId());
            updatedPlayer = pm.switchRangoUsuario(updatedPlayer, newRango);
        }
        if (!existing.getTipoUsuario().equals(obj.getTipoUsuario())) {
            TipoUsuario newTipo = TipoUsuarioRegistry.getInstance().get(obj.getTipoUsuario().getId());
            updatedPlayer = pm.switchTipoUsuario(updatedPlayer, newTipo);
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
