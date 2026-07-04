package com.bteconosur.core.api.json.api;

import com.bteconosur.db.model.Division;

public class DivisionSummaryDTO {

    private long id;
    private String nombre;
    private String nam;
    private String gna;
    private String fna;
    private String contexto;

    public DivisionSummaryDTO(Division division) {
        this.id = division.getId();
        this.nombre = division.getNombre();
        this.nam = division.getNam();
        this.gna = division.getGna();
        this.fna = division.getFna();
        this.contexto = division.getContexto();
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNam() {
        return nam;
    }

    public String getGna() {
        return gna;
    }

    public String getFna() {
        return fna;
    }

    public String getContexto() {
        return contexto;
    }
}
