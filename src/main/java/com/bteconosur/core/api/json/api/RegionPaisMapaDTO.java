package com.bteconosur.core.api.json.api;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.bteconosur.core.api.ApiUtils;
import com.bteconosur.db.model.RegionPais;

public class RegionPaisMapaDTO {

    private long id;
    private String nombre;
    private String polygon;
    private String paisNombre;

    public RegionPaisMapaDTO() {}

    public RegionPaisMapaDTO(RegionPais region, String paisNombre) {
        this.id = region.getId();
        this.nombre = region.getNombre();
        this.paisNombre = paisNombre;
        if (region.getPoligono() != null) {
            Polygon geo = ApiUtils.toGeoPolygon(region.getPoligono());
            this.polygon = (geo != null) ? new GeoJsonWriter().write(geo) : null;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPolygon() {
        return polygon;
    }

    public void setPolygon(String polygon) {
        this.polygon = polygon;
    }

    public String getPaisNombre() {
        return paisNombre;
    }

    public void setPaisNombre(String paisNombre) {
        this.paisNombre = paisNombre;
    }

}