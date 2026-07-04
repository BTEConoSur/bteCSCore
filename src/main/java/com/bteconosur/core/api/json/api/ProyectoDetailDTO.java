package com.bteconosur.core.api.json.api;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.bteconosur.core.api.ApiUtils;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.TipoProyecto;
import com.bteconosur.db.util.Estado;

public class ProyectoDetailDTO {

    private String id;
    private String nombre;
    private String descripcion;
    private Estado estado;
    private String poligono;
    private double tamaño;
    private Date fechaCreado;
    private Date fechaTerminado;
    private TipoProyecto tipoProyecto;
    private PlayerSummaryDTO lider;
    private DivisionDetailDTO division;

    public ProyectoDetailDTO(Proyecto proyecto) {
        this.id = proyecto.getId();
        this.nombre = proyecto.getNombre();
        this.descripcion = proyecto.getDescripcion();
        this.estado = proyecto.getEstado();
        Polygon geoPolygon = ApiUtils.toGeoPolygon(proyecto.getPoligono());
        this.poligono = new GeoJsonWriter().write(geoPolygon);
        this.tamaño = proyecto.getTamaño();
        this.fechaCreado = proyecto.getFechaCreado();
        this.fechaTerminado = proyecto.getFechaTerminado();
        this.tipoProyecto = proyecto.getTipoProyecto();
        if (proyecto.getLider() != null) {
            this.lider = new PlayerSummaryDTO(proyecto.getLider());
        }
        if (proyecto.getDivision() != null) {
            this.division = new DivisionDetailDTO(proyecto.getDivision());
        }
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getPoligono() {
        return poligono;
    }

    public double getTamaño() {
        return tamaño;
    }

    public Date getFechaCreado() {
        return fechaCreado;
    }

    public Date getFechaTerminado() {
        return fechaTerminado;
    }

    public TipoProyecto getTipoProyecto() {
        return tipoProyecto;
    }

    public PlayerSummaryDTO getLider() {
        return lider;
    }

    public DivisionDetailDTO getDivision() {
        return division;
    }

}
