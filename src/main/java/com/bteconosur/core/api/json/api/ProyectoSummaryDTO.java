package com.bteconosur.core.api.json.api;

import java.util.Date;

import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.TipoProyecto;
import com.bteconosur.db.util.Estado;

public class ProyectoSummaryDTO {

    private String id;
    private String nombre;
    private String descripcion;
    private Estado estado;
    private double tamaño;
    private Date fechaCreado;
    private Date fechaTerminado;
    private TipoProyecto tipoProyecto;
    private PlayerSummaryDTO lider;
    private DivisionSummaryDTO division;

    public ProyectoSummaryDTO(Proyecto proyecto) {
        this.id = proyecto.getId();
        this.nombre = proyecto.getNombre();
        this.descripcion = proyecto.getDescripcion();
        this.estado = proyecto.getEstado();
        this.tamaño = proyecto.getTamaño();
        this.fechaCreado = proyecto.getFechaCreado();
        this.fechaTerminado = proyecto.getFechaTerminado();
        this.tipoProyecto = proyecto.getTipoProyecto();
        if (proyecto.getLider() != null) {
            this.lider = new PlayerSummaryDTO(proyecto.getLider());
        }
        if (proyecto.getDivision() != null) {
            this.division = new DivisionSummaryDTO(proyecto.getDivision());
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

    public DivisionSummaryDTO getDivision() {
        return division;
    }

}
