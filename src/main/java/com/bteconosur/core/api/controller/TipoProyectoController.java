package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import java.util.stream.Collectors;

import com.bteconosur.db.model.TipoProyecto;
import com.bteconosur.db.registry.TipoProyectoRegistry;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;

public class TipoProyectoController {

    TipoProyectoRegistry tu = TipoProyectoRegistry.getInstance();

    public void registrar(RoutesConfig config) {
        config.apiBuilder(() -> {
            path("/api/tipo-proyecto", () -> {
                get(this::listar);
                post(this::crear);
                path("/{id}", () -> {
                    get(this::obtener);
                    put(this::actualizar);
                    delete(this::eliminar);
                });
            });
        });
    }

    private void listar(Context ctx) {
        ctx.json(tu.getList().stream().map(tipo -> {
            if (tipo.getTamanoMin() == null) {
                tipo.setTamanoMin(-1);
            }
            if (tipo.getTamanoMax() == null) {
                tipo.setTamanoMax(-1);
            }
            return tipo;
        }).collect(Collectors.toList()));
    }

    private void obtener(Context ctx) {
        TipoProyecto obj = tu.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("TipoProyecto not found");
            return;
        }

        if (obj.getTamanoMin() == null) {
            obj.setTamanoMin(-1);
        }

        if (obj.getTamanoMax() == null) {
            obj.setTamanoMax(-1);
        }
        ctx.json(obj);
    }

    private void crear(Context ctx) {
        TipoProyecto obj = ctx.bodyAsClass(TipoProyecto.class);
        if (obj.getTamanoMin() != null && obj.getTamanoMin() == -1) {
            obj.setTamanoMin(null);
        }

        if (obj.getTamanoMax() != null && obj.getTamanoMax() == -1) {
            obj.setTamanoMax(null);
        }

        tu.load(obj);
        ctx.status(201).json(obj);
    }

    private void actualizar(Context ctx) {
        TipoProyecto obj = ctx.bodyAsClass(TipoProyecto.class);
        TipoProyecto existing = tu.get(Long.valueOf(ctx.pathParam("id")));
        if (existing == null) {
            ctx.status(404).result("TipoProyecto not found");
            return;
        }
        existing.setNombre(obj.getNombre());
        existing.setMaxMiembros(obj.getMaxMiembros());
        existing.setTamanoMin(obj.getTamanoMin() == -1 ? null : obj.getTamanoMin());
        existing.setTamanoMax(obj.getTamanoMax() == -1 ? null : obj.getTamanoMax());
        ctx.json(tu.merge(Long.valueOf(ctx.pathParam("id"))));
    }

    private void eliminar(Context ctx) {
        TipoProyecto obj = tu.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("TipoProyecto not found");
            return;
        }
        tu.unload(Long.valueOf(ctx.pathParam("id")));
        ctx.status(204).result("TipoProyecto deleted");
    }

}
