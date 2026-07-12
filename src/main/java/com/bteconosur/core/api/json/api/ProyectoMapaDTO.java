package com.bteconosur.core.api.json.api;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.bteconosur.core.api.ApiUtils;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.Estado;

public class ProyectoMapaDTO {

    private String id;
    private String nombre;
    private Estado estado;
    private String poligono;
    private String liderNombre;

    public ProyectoMapaDTO() {
    }

    public ProyectoMapaDTO(Proyecto proyecto) {
        this.id = proyecto.getId();
        this.nombre = proyecto.getNombre();
        this.estado = proyecto.getEstado();
        if (proyecto.getPoligono() != null) {
            Polygon geoPolygon = ApiUtils.toGeoPolygon(proyecto.getPoligono());
            this.poligono = (geoPolygon != null) ? new GeoJsonWriter().write(geoPolygon) : null;
        } else {
            this.poligono = null;
        }
        this.liderNombre = proyecto.getLider() != null ? proyecto.getLider().getNombrePublico() : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getPoligono() {
        return poligono;
    }

    public void setPoligono(String poligono) {
        this.poligono = poligono;
    }

    public String getLiderNombre() {
        return liderNombre;
    }

    public void setLiderNombre(String liderNombre) {
        this.liderNombre = liderNombre;
    }

}