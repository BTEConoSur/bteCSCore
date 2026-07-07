package com.bteconosur.core.api.json.api;

import java.util.UUID;

public class FinishRequest {

    private UUID staffId;
    private String comentario;
    private Boolean promote;

    public FinishRequest() {
    }

    public FinishRequest(UUID staffId, String comentario, Boolean promote) {
        this.staffId = staffId;
        this.comentario = comentario;
        this.promote = promote;
    }

    public UUID getStaffId() {
        return staffId;
    }

    public void setStaffId(UUID staffId) {
        this.staffId = staffId;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Boolean getPromote() {
        return promote;
    }

    public void setPromote(Boolean promote) {
        this.promote = promote;
    }
    
}
