package com.bteconosur.core.api.json.bteweb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representa una solicitud de reclamación (claim) a enviar a la API web de BTE.
 * Contiene la información necesaria para crear o actualizar un claim en el servidor web.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaimRequest {

    private UserRef owner;
    private List<List<String>> area;
    private boolean active;
    private boolean finished;
    private String name;
    private String externalId;
    private String description;
    private String city;
    private List<UserRef> builders;

    public UserRef getOwner() {
        return owner;
    }

    public void setOwner(UserRef owner) {
        this.owner = owner;
    }

    public List<List<String>> getArea() {
        return area;
    }

    public void setArea(List<List<String>> area) {
        this.area = area;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<UserRef> getBuilders() {
        return builders;
    }

    public void setBuilders(List<UserRef> builders) {
        this.builders = builders;
    }
}
