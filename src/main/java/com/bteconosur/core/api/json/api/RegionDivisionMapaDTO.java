package com.bteconosur.core.api.json.api;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.bteconosur.core.api.ApiUtils;
import com.bteconosur.db.model.RegionDivision;

public class RegionDivisionMapaDTO {

    private long id;
    private String nombre;
    private String polygon;
    private String divisionNombre;

    public RegionDivisionMapaDTO() {}

    public RegionDivisionMapaDTO(RegionDivision region, String divisionNombre) {
        this.id = region.getId();
        this.nombre = region.getNombre();
        this.divisionNombre = divisionNombre;
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

    public String getDivisionNombre() {
        return divisionNombre;
    }

    public void setDivisionNombre(String divisionNombre) {
        this.divisionNombre = divisionNombre;
    }
}