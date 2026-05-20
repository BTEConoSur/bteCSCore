package com.bteconosur.core.api.json.bteweb;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representa una referencia compacta a un usuario en el sistema de la API web de BTE.
 * Contiene el identificador único del usuario necesario para asociaciones en claims y proyectos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRef {
    
    private UUID id;
    /*
    private UUID ssoId;
    private Long discordId;
    private String name;
    */

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    /*
    public UUID getSsoId() {
        return ssoId;
    }
    public void setSsoId(UUID ssoId) {
        this.ssoId = ssoId;
    }
    public Long getDiscordId() {
        return discordId;
    }
    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    */
     

}
