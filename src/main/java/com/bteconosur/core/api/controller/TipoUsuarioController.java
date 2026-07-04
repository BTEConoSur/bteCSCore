package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.*;

import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.TipoUsuarioRegistry;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;

public class TipoUsuarioController {
    
    TipoUsuarioRegistry tu = TipoUsuarioRegistry.getInstance();

    public void registrar(RoutesConfig config) {
        config.apiBuilder(() -> {
            path("/api/tipo-usuario", () -> {
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
        ctx.json(tu.getList());
    }

    private void obtener(Context ctx) {
        TipoUsuario obj = tu.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("TipoUsuario not found");
            return;
        }
        ctx.json(obj);
    }

    private void crear(Context ctx) {
        TipoUsuario obj = ctx.bodyAsClass(TipoUsuario.class);
        tu.load(obj);
        ctx.status(201).json(obj); //TODO: Errores
    }

    private void actualizar(Context ctx) {
        TipoUsuario obj = ctx.bodyAsClass(TipoUsuario.class);
        TipoUsuario existing = tu.get(Long.valueOf(ctx.pathParam("id")));
        if (existing == null) {
            ctx.status(404).result("TipoUsuario not found");
            return;
        }
        existing.setNombre(obj.getNombre());
        existing.setDescripcion(obj.getDescripcion());
        existing.setCantProyecSim(obj.getCantProyecSim());
    
        ctx.json(tu.merge(Long.valueOf(ctx.pathParam("id"))));
    }

    private void eliminar(Context ctx) {
        TipoUsuario obj = tu.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("TipoUsuario not found");
            return;
        }
        tu.unload(Long.valueOf(ctx.pathParam("id")));
        ctx.status(204).result("TipoUsuario deleted");
    }

}
