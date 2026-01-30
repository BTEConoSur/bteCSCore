package com.bteconosur.db.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "division")
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_division", nullable = false)
    private Long id;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

     @Column(name = "nombre_publico", length = 100, nullable = false)
    private String nombrePublico;

    @Column(name = "tipo_division", length = 100)
    private String tipoDivision;

    @Column(name = "contexto", length = 100)
    private String contexto;

    @ManyToOne
    @JoinColumn(name = "id_pais")
    private Pais pais;

    @OneToMany(mappedBy = "division", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    private List<RegionDivision> regiones = new ArrayList<>();

    @OneToMany(mappedBy = "division")
    @JsonIgnore
    private Set<Proyecto> proyectos = new HashSet<>();

    public Division() {
    }

    public Division(Pais pais, String nombre, String nombrePublico, String tipoDivision, String contexto) {
        this.nombre = nombre;
        this.nombrePublico = nombrePublico;
        this.tipoDivision = tipoDivision;
        this.contexto = contexto;
        this.pais = pais;
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

    public String getNombrePublico() {
        return nombrePublico;
    }

    public void setNombrePublico(String nombrePublico) {
        this.nombrePublico = nombrePublico;
    }

    public String getTipoDivision() {
        return tipoDivision;
    }

    public void setTipoDivision(String tipoDivision) {
        this.tipoDivision = tipoDivision;
    }

    public String getContexto() {
        return contexto;
    }

    public void setContexto(String contexto) {
        this.contexto = contexto;
    }

    public List<RegionDivision> getRegiones() {
        return regiones;
    }

    public void setRegiones(List<RegionDivision> regiones) {
        this.regiones = regiones;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public Set<Proyecto> getProyectos() {
        return proyectos;
    }

    public void setProyectos(Set<Proyecto> proyectos) {
        this.proyectos = proyectos;
    }

}
