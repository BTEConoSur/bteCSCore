package com.bteconosur.db.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;

@Entity
@Table(name = "pais")
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id_pais", nullable = false)
    private Long id;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "ds_id_guild", nullable = false)
    private Long dsIdGuild;

    @Column(name = "ds_id_global_chat", nullable = false)
    private Long dsIdGlobalChat;

    @Column(name = "ds_id_country_chat", nullable = false)
    private Long dsIdCountryChat;

    @Column(name = "ds_id_log", nullable = false)
    private Long dsIdLog;

    @Column(name = "ds_id_request", nullable = false)
    private Long dsIdRequest;

    @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<RegionPais> regiones = new ArrayList<>();

    @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Ciudad> ciudades = new ArrayList<>();

    @ManyToMany(mappedBy = "paisesManager")
    @JsonIgnore
    private Set<Player> managers = new HashSet<>();

    @ManyToMany(mappedBy = "paisesReviewer")
    @JsonIgnore
    private Set<Player> reviewers = new HashSet<>();

    @OneToMany(mappedBy = "paisPrefix")
    @JsonIgnore
    private List<Player> jugadoresPrefix = new ArrayList<>();

    public Pais() {
    }

    public Pais(String nombre, Long dsIdGuild, Long dsIdGlobalChat, Long dsIdCountryChat, Long dsIdLog, Long dsIdRequest) {
        this.nombre = nombre;
        this.dsIdGuild = dsIdGuild;
        this.dsIdGlobalChat = dsIdGlobalChat;
        this.dsIdCountryChat = dsIdCountryChat;
        this.dsIdLog = dsIdLog;
        this.dsIdRequest = dsIdRequest;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getDsIdGuild() {
        return dsIdGuild;
    }

    public void setDsIdGuild(Long dsIdGuild) {
        this.dsIdGuild = dsIdGuild;
    }

    public Long getDsIdGlobalChat() {
        return dsIdGlobalChat;
    }

    public void setDsIdGlobalChat(Long dsIdGlobalChat) {
        this.dsIdGlobalChat = dsIdGlobalChat;
    }

    public Long getDsIdCountryChat() {
        return dsIdCountryChat;
    }

    public void setDsIdCountryChat(Long dsIdCountryChat) {
        this.dsIdCountryChat = dsIdCountryChat;
    }

    public Long getDsIdLog() {
        return dsIdLog;
    }

    public void setDsIdLog(Long dsIdLog) {
        this.dsIdLog = dsIdLog;
    }

    public Long getDsIdRequest() {
        return dsIdRequest;
    }

    public void setDsIdRequest(Long dsIdRequest) {
        this.dsIdRequest = dsIdRequest;
    }

    public List<RegionPais> getRegiones() {
        return regiones;
    }

    public void setRegiones(List<RegionPais> regiones) {
        this.regiones = regiones;
    }

    public List<Ciudad> getCiudades() {
        return ciudades;
    }

    public void setCiudades(List<Ciudad> ciudades) {
        this.ciudades = ciudades;
    }

    public Set<Player> getManagers() {
        return managers;
    }

    public void setManagers(Set<Player> managers) {
        this.managers = managers;
    }

    public Set<Player> getReviewers() {
        return reviewers;
    }

    public void setReviewers(Set<Player> reviewers) {
        this.reviewers = reviewers;
    }

    public List<Player> getJugadoresPrefix() {
        return jugadoresPrefix;
    }

    public void setJugadoresPrefix(List<Player> jugadoresPrefix) {
        this.jugadoresPrefix = jugadoresPrefix;
    }

}
//TODO: Ver casos de migracion de datos