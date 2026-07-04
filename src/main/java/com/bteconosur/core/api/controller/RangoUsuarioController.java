package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.*;

import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.registry.RangoUsuarioRegistry;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;

public class RangoUsuarioController {

    RangoUsuarioRegistry ru = RangoUsuarioRegistry.getInstance();

    public void registrar(RoutesConfig config) {
        config.apiBuilder(() -> {
            path("/api/rango-usuario", () -> {
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
        ctx.json(ru.getList());
    }

    private void obtener(Context ctx) {
        RangoUsuario obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("RangoUsuario not found");
            return;
        }
        ctx.json(obj);
    }

    private void crear(Context ctx) {
        RangoUsuario obj = ctx.bodyAsClass(RangoUsuario.class);
        ru.load(obj);
        ctx.status(201).json(obj);
    }

    private void actualizar(Context ctx) {
        RangoUsuario obj = ctx.bodyAsClass(RangoUsuario.class);
        RangoUsuario existing = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (existing == null) {
            ctx.status(404).result("RangoUsuario not found");
            return;
        }
        existing.setNombre(obj.getNombre());
    
        ctx.json(ru.merge(Long.valueOf(ctx.pathParam("id"))));
    }

    private void eliminar(Context ctx) {
        RangoUsuario obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("RangoUsuario not found");
            return;
        }
        ru.unload(Long.valueOf(ctx.pathParam("id")));
        ctx.status(204).result("RangoUsuario deleted");
    }

}
