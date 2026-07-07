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

    public ProyectoSummaryDTO() {
    }

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

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Estado getEstado() {
        return estado;
    }
    
    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public double getTamaño() {
        return tamaño;
    }

    public void setTamaño(double tamaño) {
        this.tamaño = tamaño;
    }

    public Date getFechaCreado() {
        return fechaCreado;
    }

    public void setFechaCreado(Date fechaCreado) {
        this.fechaCreado = fechaCreado;
    }

    public Date getFechaTerminado() {
        return fechaTerminado;
    }

    public void setFechaTerminado(Date fechaTerminado) {
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

    public DivisionSummaryDTO getDivision() {
        return division;
    }

    public void setDivision(DivisionSummaryDTO division) {
        this.division = division;
    }

}
