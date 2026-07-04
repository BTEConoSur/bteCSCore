package com.bteconosur.core.api.json.api;

import com.bteconosur.db.model.RegionPais;

public class RegionPaisSummaryDTO {

    private long id;
    private String nombre;

    public RegionPaisSummaryDTO(RegionPais region) {
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
