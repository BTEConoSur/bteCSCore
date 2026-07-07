package com.bteconosur.core.api.json.api;

import com.bteconosur.db.model.Pais;

public class PaisSummaryDTO {

    private long id;
    private String nombre;
    private String nombrePublico;

    public PaisSummaryDTO() {
    }

    public PaisSummaryDTO(Pais p) {
        this.id = p.getId();
        this.nombre = p.getNombre();
        this.nombrePublico = p.getNombrePublico();
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

    public String getNombrePublico() {
        return nombrePublico;
    }

    public void setNombrePublico(String nombrePublico) {
        this.nombrePublico = nombrePublico;
    }

}
