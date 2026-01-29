package com.bteconosur.db.model;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "tipo_division", length = 50)
    private String tipoDivision;

    @Column(name = "contexto", length = 100)
    private String contexto;

    @Column(name = "poligono")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @JsonIgnore // evita que Jackson intente serializar la geometría cruda
    private MultiPolygon poligono;

    @ManyToOne
    @JoinColumn(name = "id_pais")
    private Pais pais;

    @OneToMany(mappedBy = "division")
    @JsonIgnore
    private Set<Proyecto> proyectos = new HashSet<>();

    public Division() {
    }

    public Division(Pais pais, String nombre, String tipoDivision, String contexto, MultiPolygon poligono) {
        this.nombre = nombre;
        this.tipoDivision = tipoDivision;
        this.contexto = contexto;
        this.poligono = poligono;
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

    public MultiPolygon getPoligono() {
        return poligono;
    }

    public void setPoligono(MultiPolygon poligono) {
        this.poligono = poligono;
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
