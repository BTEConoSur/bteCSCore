package com.bteconosur.core.api.json.api;

import com.bteconosur.db.model.RegionDivision;

public class RegionDivisionSummaryDTO {

    private long id;
    private String nombre;

    public RegionDivisionSummaryDTO() {
    }

    public RegionDivisionSummaryDTO(RegionDivision region) {
        this.id = region.getId();
        this.nombre = region.getNombre();
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

}
