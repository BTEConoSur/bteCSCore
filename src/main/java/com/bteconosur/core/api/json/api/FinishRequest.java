package com.bteconosur.core.api.json.api;

import java.util.UUID;

public class FinishRequest {

    private UUID staffId;
    private String comentario;
    private Boolean promote;

    public FinishRequest(UUID staffId, String comentario, Boolean promote) {
        this.staffId = staffId;
        this.comentario = comentario;
        this.promote = promote;
    }

    public UUID getStaffId() {
        return staffId;
    }

    public String getComentario() {
        return comentario;
    }

    public Boolean getPromote() {
        return promote;
    }
    
}
