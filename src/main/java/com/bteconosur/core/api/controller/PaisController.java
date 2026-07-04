package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import java.util.List;

import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.RegionPais;
import com.bteconosur.db.registry.PaisRegistry;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;

public class PaisController {

    PaisRegistry ru = PaisRegistry.getInstance();

    public void registrar(RoutesConfig config) {
        config.apiBuilder(() -> {
            path("/api/pais", () -> {
                get(this::listar);
                post(this::crear);
                path("/{id}", () -> {
                    get(this::obtener);
                    put(this::actualizar);
                    delete(this::eliminar);
                    path("/regiones", () -> {
                        get(this::listarRegiones);
                        put(this::añadirRegion);
                        path("/{regionId}", () -> {
                            delete(this::eliminarRegion);
                        });
                    });
                });
                
            });
        });
    }

    private void listar(Context ctx) {
        ctx.json(ru.getList());
    }

    private void obtener(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        ctx.json(obj);
    }

    private void crear(Context ctx) {
        Pais obj = ctx.bodyAsClass(Pais.class);
        ru.load(obj);
        ctx.status(201).json(obj);
    }

    private void actualizar(Context ctx) {
        Pais obj = ctx.bodyAsClass(Pais.class);
        Pais existing = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (existing == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        existing.setNombre(obj.getNombre());
    
        ctx.json(ru.merge(Long.valueOf(ctx.pathParam("id"))));
    }

    private void eliminar(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        ru.unload(Long.valueOf(ctx.pathParam("id")));
        ctx.status(204).result("Pais deleted");
    }
    
    private void listarRegiones(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        List<RegionPais> regiones = ru.getRegions(obj);
        ctx.json(regiones);
    }

    private void añadirRegion(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        RegionPais region = ctx.bodyAsClass(RegionPais.class);
        obj.
        ctx.status(201).json(region);
    }

}
