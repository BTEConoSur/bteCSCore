package com.bteconosur.db.model;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_proyecto")
public class TipoProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id_tipo_proyecto", nullable = false)
    private Long id;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "max_miembros", nullable = false)
    private Integer maxMiembros;

    @Column(name = "tamano_min", nullable = false)
    private Integer tamanoMin;

    @Column(name = "tamano_max", nullable = false)
    private Integer tamanoMax;

    @OneToMany(mappedBy = "tipoProyecto")
    @JsonIgnore
    private Set<Proyecto> proyectos = new HashSet<>();

    public TipoProyecto() {
    }

    public TipoProyecto(String nombre, Integer maxMiembros, Integer tamanoMin, Integer tamanoMax) {
        this.nombre = nombre;
        this.maxMiembros = maxMiembros;
        this.tamanoMin = tamanoMin;
        this.tamanoMax = tamanoMax;
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

    public Integer getMaxMiembros() {
        return maxMiembros;
    }   

    public void setMaxMiembros(Integer maxMiembros) {
        this.maxMiembros = maxMiembros;
    }

    public Integer getTamanoMin() {
        return tamanoMin;
    }

    public void setTamanoMin(Integer tamanoMin) {
        this.tamanoMin = tamanoMin;
    }   

    public Integer getTamanoMax() {
        return tamanoMax;
    }   

    public void setTamanoMax(Integer tamanoMax) {
        this.tamanoMax = tamanoMax;
    }

    public Set<Proyecto> getProyectos() {
        return proyectos;
    }

    public void setProyectos(Set<Proyecto> proyectos) {
        this.proyectos = proyectos;
    }

}
