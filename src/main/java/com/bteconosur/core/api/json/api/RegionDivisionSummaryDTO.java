package com.bteconosur.core.api.json.api;

import com.bteconosur.db.model.RegionDivision;

public class RegionDivisionSummaryDTO {

    private long id;
    private String nombre;

    public RegionDivisionSummaryDTO(RegionDivision region) {
        this.id = region.getId();
        this.nombre = region.getNombre();
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

}
