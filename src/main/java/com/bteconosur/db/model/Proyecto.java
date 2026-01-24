package com.bteconosur.db.model;

import org.locationtech.jts.geom.Polygon;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bteconosur.db.util.Estado;
import com.bteconosur.db.util.IDUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "proyecto")
public class Proyecto {

    @Id
    @Column(name = "id_proyecto", columnDefinition = "CHAR(6)", length = 6, nullable = false)
    private String id;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING) 
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "poligono")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Polygon poligono;

    @ManyToOne
    @JoinColumn(name = "id_tipo_proyecto")
    private TipoProyecto tipoProyecto;

    @ManyToOne
    @JoinColumn(name = "uuid_lider")
    private Player lider;

    @ManyToOne
    @JoinColumn(name = "id_ciudad")
    private Ciudad ciudad;

    @ManyToMany(mappedBy = "proyectos", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Player> miembros = new HashSet<>(); // Players detached, usar ProjectManager para obtener miembros.

    public Proyecto() {
    }

    public Proyecto(String nombre, String descripcion, Estado estado, Polygon poligono) {
        this.id = IDUtils.generarCodigoProyecto();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.poligono = poligono;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Polygon getPoligono() {
        return poligono;
    }

    public void setPoligono(Polygon poligono) {
        this.poligono = poligono;
    }

    public TipoProyecto getTipoProyecto() {
        return tipoProyecto;
    }

    public void setTipoProyecto(TipoProyecto tipoProyecto) {
        this.tipoProyecto = tipoProyecto;
    }

    public Player getLider() {
        return lider;
    }

    public void setLider(Player lider) {
        this.lider = lider;
    }

    public Set<Player> getMiembros() {
        return miembros;
    }

    public void setMiembros(Set<Player> miembros) {
        this.miembros = miembros;
    }

    public Ciudad getCiudad() {
        return ciudad;
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public Pais getPais() {
        return ciudad.getPais();
    }

    public void addMiembro(Player player) {
        this.miembros.add(player);
    }

    public void removeMiembro(Player player) {
        this.miembros.remove(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proyecto proyecto = (Proyecto) o;
        if (id == null || proyecto.id == null) return false;
        return Objects.equals(id, proyecto  .id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
