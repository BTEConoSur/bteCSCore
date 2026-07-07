package com.bteconosur.core.api.json.api;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.bteconosur.core.api.ApiUtils;
import com.bteconosur.db.model.RegionDivision;

public class RegionDivisionDetailDTO {

    private long id;
    private String nombre;
    private String polygon;

    public RegionDivisionDetailDTO() {
    }

    public RegionDivisionDetailDTO(RegionDivision region) {
        this.id = region.getId();
        this.nombre = region.getNombre();
        Polygon geoPolygon = ApiUtils.toGeoPolygon(region.getPoligono());
        this.polygon = new GeoJsonWriter().write(geoPolygon);
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

}
