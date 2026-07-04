package com.bteconosur.core.api.json.api;

import com.bteconosur.db.model.Pais;

public class PaisSummaryDTO {

    private long id;
    private String nombre;
    private String nombrePublico;

    public PaisSummaryDTO(Pais p) {
        this.id = p.getId();
        this.nombre = p.getNombre();
        this.nombrePublico = p.getNombrePublico();
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombrePublico() {
        return nombrePublico;
    }

}
