package com.bteconosur.core.api.json.bteweb;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

/**
 * Representa una reclamación (claim) de proyecto retornada por la API web de BTE.
 * Contiene información completa del claim incluyendo metadatos, ubicación, estado y estadísticas.
 */
public class Claim {

    private UUID id;
    private String externalId;
    private UUID ownerId;
    private List<String> area;
    private String center;
    private int size;
    private int buildings;
    private boolean active;
    private boolean finished;
    private String name;
    private String description;
    private String city;
    private String osmName;
    private UUID buildTeamId;
    private Date createdAt;
    
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getExternalId() {
        return externalId;
    }
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    public UUID getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    public List<String> getArea() {
        return area;
    }
    public void setArea(List<String> area) {
        this.area = area;
    }
    public String getCenter() {
        return center;
    }
    public void setCenter(String center) {
        this.center = center;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public int getBuildings() {
        return buildings;
    }
    public void setBuildings(int buildings) {
        this.buildings = buildings;
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
    public String getOsmName() {
        return osmName;
    }
    public void setOsmName(String osmName) {
        this.osmName = osmName;
    }
    public UUID getBuildTeamId() {
        return buildTeamId;
    }
    public void setBuildTeamId(UUID buildTeamId) {
        this.buildTeamId = buildTeamId;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
