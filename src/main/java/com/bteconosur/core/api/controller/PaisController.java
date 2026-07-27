package com.bteconosur.core.api.controller;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import com.bteconosur.core.api.ApiUtils;
import com.bteconosur.core.api.json.api.DivisionSummaryDTO;
import com.bteconosur.core.api.json.api.PaginaDTO;
import com.bteconosur.core.api.json.api.RegionPaisDetailDTO;
import com.bteconosur.core.api.json.api.RegionPaisMapaDTO;
import com.bteconosur.core.api.json.api.RegionPaisSummaryDTO;
import com.bteconosur.db.model.Division;
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
                path("/regiones/mapa", () -> {
                    get(this::listarTodasLasRegionesMapa);
                });
                path("/{id}", () -> {
                    get(this::obtener);
                    put(this::actualizar);
                    delete(this::eliminar);
                    path("/regiones-mapa", () -> {
                        get(this::listarRegionesMapaDePais);
                    });
                    path("/regiones", () -> {
                        get(this::listarRegiones);
                        put(this::añadirRegion);
                        path("/{regionId}", () -> {
                            get(this::obtenerRegion);
                            delete(this::eliminarRegion);
                        });
                    });
                    path("/divisiones", () -> {
                        get(this::listarDivisiones);
                    });
                });
                
            });
        });
    }

    private void listarTodasLasRegionesMapa(Context ctx) {
        List<RegionPaisMapaDTO> resultado = new ArrayList<>();
        for (Pais pais : ru.getList()) {
            List<RegionPais> regiones = ru.getRegions(pais);
            if (regiones == null) continue;
            for (RegionPais region : regiones) {
                resultado.add(new RegionPaisMapaDTO(region, pais.getNombre()));
            }
        }
        ctx.json(resultado);
    }

    private void listarRegionesMapaDePais(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        List<RegionPais> regiones = ru.getRegions(obj);
        if (regiones == null) {
            ctx.json(List.of());
            return;
        }
        List<RegionPaisMapaDTO> resultado = regiones.stream()
            .map(r -> new RegionPaisMapaDTO(r, obj.getNombre()))
            .toList();
        ctx.json(resultado);
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
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = Math.min(ctx.queryParamAsClass("size", Integer.class).getOrDefault(20), 20);
        List<RegionPais> regiones = ru.getRegions(obj);
        if (regiones == null) {
            ctx.json(new PaginaDTO<RegionPaisSummaryDTO>(List.of(), page, size, 0));
            return;
        }
        
        int total = regiones.size();
        int desde = Math.min(page * size, total);
        int hasta = Math.min(desde + size, total);

        List<RegionPaisSummaryDTO> pagina = regiones.subList(desde, hasta).stream().map(RegionPaisSummaryDTO::new).toList();

        ctx.json(new PaginaDTO<RegionPaisSummaryDTO>(pagina, page, size, total));
    }

    private void añadirRegion(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        RegionPaisDetailDTO region = ctx.bodyAsClass(RegionPaisDetailDTO.class);
        RegionPais regionEntity = new RegionPais();
        regionEntity.setNombre(region.getNombre());
        regionEntity.setPais(obj);
        GeoJsonReader reader = new GeoJsonReader();
        try {
            Geometry geometryGeo = reader.read(region.getPolygon());
            Polygon mcPolygon = ApiUtils.toMcPolygon((Polygon) geometryGeo);

            if (mcPolygon == null) {
                ctx.status(400).result("No se pudo convertir el polígono a coordenadas de Minecraft");
                return;
            }
            regionEntity.setPoligono(mcPolygon);
        } catch (Exception e) {
            ctx.status(400).result("Formato de geometría inválido: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        ru.addRegionPais(regionEntity);
        ctx.status(201).json(new RegionPaisDetailDTO(regionEntity));
    }

    public void obtenerRegion(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        Long regionId = Long.valueOf(ctx.pathParam("regionId"));
        RegionPais region = ru.getRegion(obj, regionId);
        if (region == null) {
            ctx.status(404).result("RegionPais not found for this Pais");
            return;
        }
        ctx.json(new RegionPaisDetailDTO(region));
    }

    public void eliminarRegion(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        Long regionId = Long.valueOf(ctx.pathParam("regionId"));
        RegionPais region = ru.getRegion(obj, regionId);
        if (region == null) {
            ctx.status(404).result("RegionPais not found for this Pais");
            return;
        }
        ru.removeRegionPais(region);
        ctx.status(204).result("RegionPais deleted");
    }

    private void listarDivisiones(Context ctx) {
        Pais obj = ru.get(Long.valueOf(ctx.pathParam("id")));
        if (obj == null) {
            ctx.status(404).result("Pais not found");
            return;
        }
        List<Division> divisions = ru.getDivisions(obj);
        if (divisions == null) {
            ctx.status(404).result("No divisions found for this Pais");
            return;
        }
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = Math.min(ctx.queryParamAsClass("size", Integer.class).getOrDefault(20), 20);

        int total = divisions.size();
        int desde = Math.min(page * size, total);
        int hasta = Math.min(desde + size, total);

        List<DivisionSummaryDTO> pagina = divisions.subList(desde, hasta).stream().map(DivisionSummaryDTO::new).toList();

        ctx.json(new PaginaDTO<DivisionSummaryDTO>(pagina, page, size, total));
    }
//TODO: Revisar cuando se crea que tenga division default
}
