package com.bteconosur.core.api.json.api;

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
    private Long fechaCreado;
    private Long fechaTerminado;
    private TipoProyecto tipoProyecto;
    private PlayerSummaryDTO lider;
    private DivisionDetailDTO division;

    public ProyectoDetailDTO() {
    }

    public ProyectoDetailDTO(Proyecto proyecto) {
        this.id = proyecto.getId();
        this.nombre = proyecto.getNombre() == null ? "" : proyecto.getNombre();
        this.descripcion = proyecto.getDescripcion() == null ? "" : proyecto.getDescripcion();
        this.estado = proyecto.getEstado();
        Polygon geoPolygon = ApiUtils.toGeoPolygon(proyecto.getPoligono());
        this.poligono = new GeoJsonWriter().write(geoPolygon);
        this.tamaño = proyecto.getTamaño();
        this.fechaCreado = proyecto.getFechaCreado() != null ? proyecto.getFechaCreado().getTime() : null;
        this.fechaTerminado = proyecto.getFechaTerminado() != null ? proyecto.getFechaTerminado().getTime() : null;
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

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? "" : nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion;
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

    public double getTamaño() {
        return tamaño;
    }

    public void setTamaño(double tamaño) {
        this.tamaño = tamaño;
    }

    public Long getFechaCreado() {
        return fechaCreado;
    }

    public void setFechaCreado(Long fechaCreado) {
        this.fechaCreado = fechaCreado;
    }

    public Long getFechaTerminado() {
        return fechaTerminado;
    }

    public void setFechaTerminado(Long fechaTerminado) {
        this.fechaTerminado = fechaTerminado;
    }

    public TipoProyecto getTipoProyecto() {
        return tipoProyecto;
    }

    public void setTipoProyecto(TipoProyecto tipoProyecto) {
        this.tipoProyecto = tipoProyecto;
    }

    public PlayerSummaryDTO getLider() {
        return lider;
    }

    public void setLider(PlayerSummaryDTO lider) {
        this.lider = lider;
    }

    public DivisionDetailDTO getDivision() {
        return division;
    }

    public void setDivision(DivisionDetailDTO division) {
        this.division = division;
    }

}
