package com.bteconosur.db.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "nodo_permiso")
public class NodoPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso", nullable = false)
    private Long id;

    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    private String nombre;

    @ManyToMany(mappedBy = "permisos")
    @JsonIgnore
    private Set<RangoUsuario> rangos = new HashSet<>();

    @ManyToMany(mappedBy = "permisos")
    @JsonIgnore
    private Set<TipoUsuario> tipos = new HashSet<>();

    public NodoPermiso() {
    }

    public NodoPermiso(String nombre) {
        this.nombre = nombre;
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

    public Set<RangoUsuario> getRangos() {
        return rangos;
    }

    public void setRangos(Set<RangoUsuario> rangos) {
        this.rangos = rangos;
    }

    public Set<TipoUsuario> getTipos() {
        return tipos;
    }

    public void setTipos(Set<TipoUsuario> tipos) {
        this.tipos = tipos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodoPermiso that = (NodoPermiso) o;
        if (id != null && that.id != null) return Objects.equals(id, that.id);
        return Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
}
