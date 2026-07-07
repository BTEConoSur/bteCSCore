package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import com.bteconosur.core.api.json.api.DivisionDetailDTO;
import com.bteconosur.core.api.json.api.PaginaDTO;
import com.bteconosur.core.api.json.api.RegionDivisionDetailDTO;
import com.bteconosur.core.api.json.api.RegionDivisionSummaryDTO;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.RegionDivision;
import com.bteconosur.db.registry.PaisRegistry;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;

public class DivisionController {

    PaisRegistry ru = PaisRegistry.getInstance();

    public void registrar(RoutesConfig config) {
        config.apiBuilder(() -> {
            path("/api/division", () -> {
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
                            get(this::obtenerRegion);
                            delete(this::eliminarRegion);
                        });
                    });
                });
                
            });
        });
    }

    private void listar(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = Math.min(ctx.queryParamAsClass("size", Integer.class).getOrDefault(20), 20);

        List<Division> todos = ru.getDivisions();
        int total = todos.size();
        int desde = Math.min(page * size, total);
        int hasta = Math.min(desde + size, total);

        List<DivisionDetailDTO> pagina = todos.subList(desde, hasta).stream().map(DivisionDetailDTO::new).toList();

        ctx.json(new PaginaDTO<DivisionDetailDTO>(pagina, page, size, total));
    }

    private void obtener(Context ctx) {
        Division obj = ru.findDivisionById(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Division not found");
            return;
        }
        ctx.json(new DivisionDetailDTO(obj));
    }

    private void crear(Context ctx) {
        DivisionDetailDTO obj = ctx.bodyAsClass(DivisionDetailDTO.class);
        Pais pais = ru.get(obj.getPais().getId());
        Division division = new Division();
        division.setNombre(obj.getNombre());
        division.setNam(obj.getNam());  
        division.setGna(obj.getGna());
        division.setFna(obj.getFna());
        division.setContexto(obj.getContexto());
        division.setPais(pais);
        ru.addDivision(division);
        ctx.status(201).json(new DivisionDetailDTO(division));
    }

    private void actualizar(Context ctx) {
        DivisionDetailDTO obj = ctx.bodyAsClass(DivisionDetailDTO.class);
        Division existing = ru.findDivisionById(Long.valueOf(ctx.pathParam("id")));
        if (existing == null) {
            ctx.status(404).result("Division not found");
            return;
        }
        Long newPaisId = obj.getPais().getId();
        existing.setNombre(obj.getNombre());
        existing.setNam(obj.getNam());
        existing.setGna(obj.getGna());
        existing.setFna(obj.getFna());
        existing.setContexto(obj.getContexto());
        ru.mergeDivision(existing, existing.getPais().getId().equals(newPaisId) ? null : newPaisId);
        ctx.json(new DivisionDetailDTO(existing));
    }

    private void eliminar(Context ctx) {
        Division obj = ru.findDivisionById(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Division not found");
            return;
        }
        ru.removeDivision(obj);
        ctx.status(204).result("Division deleted");
    }
    
    private void listarRegiones(Context ctx) {
        Division obj = ru.findDivisionById(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Division not found");
            return;
        }
        List<RegionDivision> regiones = ru.getRegionDivisions(obj);
        if (regiones == null) {
            ctx.status(404).result("No regions found for this Division");
            return;
        }
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = Math.min(ctx.queryParamAsClass("size", Integer.class).getOrDefault(20), 20);

        int total = regiones.size();
        int desde = Math.min(page * size, total);
        int hasta = Math.min(desde + size, total);

        List<RegionDivisionSummaryDTO> pagina = regiones.subList(desde, hasta).stream().map(RegionDivisionSummaryDTO::new).toList();

        ctx.json(new PaginaDTO<RegionDivisionSummaryDTO>(pagina, page, size, total));
    }

    private void añadirRegion(Context ctx) {
        Division obj = ru.findDivisionById(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Division not found");
            return;
        }
        RegionDivisionDetailDTO region = ctx.bodyAsClass(RegionDivisionDetailDTO.class);
        RegionDivision regionEntity = new RegionDivision();
        regionEntity.setNombre(region.getNombre());
        GeoJsonReader reader = new GeoJsonReader();
        Geometry geometry;
        try {
            geometry = reader.read(region.getPolygon());
            regionEntity.setPoligono((Polygon) geometry);
        } catch (Exception e) {
            ctx.status(400).result("Invalid geometry format");
            e.printStackTrace();
        }
        
        ru.addRegionDivision(regionEntity);
        ctx.status(201).json(new RegionDivisionDetailDTO(regionEntity));
    }

    public void obtenerRegion(Context ctx) {
        Division obj = ru.findDivisionById(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Division not found");
            return;
        }
        Long regionId = Long.valueOf(ctx.pathParam("regionId"));
        RegionDivision region = ru.getRegionDivision(obj, regionId);
        if (region == null) {
            ctx.status(404).result("RegionDivision not found for this Division");
            return;
        }
        ctx.json(new RegionDivisionDetailDTO(region));
    }

    public void eliminarRegion(Context ctx) {
        Division obj = ru.findDivisionById(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Division not found");
            return;
        }
        Long regionId = Long.valueOf(ctx.pathParam("regionId"));
        RegionDivision region = ru.getRegionDivision(obj, regionId);
        if (region == null) {
            ctx.status(404).result("RegionDivision not found for this Division");
            return;
        }
        ru.removeRegionDivision(region);
        ctx.status(204).result("RegionDivision deleted");
    }
}
