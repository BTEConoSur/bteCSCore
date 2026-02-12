package com.bteconosur.db.model;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.Date;
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
import jakarta.persistence.JoinTable;
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

    @Column(name = "tamaño")
    private double tamaño;

    @Column(name = "f_creado", nullable = false)
    private Date fechaCreado;

    @Column(name = "f_terminado")
    private Date fechaTerminado;

    @ManyToOne
    @JoinColumn(name = "id_tipo_proyecto")
    private TipoProyecto tipoProyecto;

    @ManyToOne
    @JoinColumn(name = "uuid_lider")
    private Player lider;

    @ManyToOne
    @JoinColumn(name = "id_division")
    private Division division;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "proyecto_miembro", joinColumns = @JoinColumn(name = "id_proyecto"), inverseJoinColumns = @JoinColumn(name = "uuid_player"))
    @JsonIgnore
    private Set<Player> miembros = new HashSet<>(); // Players detached, usar ProjectManager para obtener miembros.

    public Proyecto() {
    }

    public Proyecto(String nombre, String descripcion, Estado estado, Polygon poligono, Double tamaño, TipoProyecto tipoProyecto, Player lider, Division division, Date fechaCreado) {
        this.id = IDUtils.generarCodigoProyecto();
        if (nombre == null) nombre = this.id;
        else this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.poligono = poligono;
        this.tamaño = tamaño;
        this.tipoProyecto = tipoProyecto;
        this.lider = lider;
        this.division = division;
        this.fechaCreado = fechaCreado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id.toUpperCase() : null;
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

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Pais getPais() {
        if (division == null) return null;
        return division.getPais();
    }

    public void addMiembro(Player player) {
        this.miembros.add(player);
    }

    public void removeMiembro(Player player) {
        this.miembros.remove(player);
    }

    public double getTamaño() {
        return tamaño;
    }

    public void updateTamaño() {
        this.tamaño = poligono.getArea();
    }

    public Date getFechaCreado() {
        return fechaCreado;
    }

    public void setFechaCreado(Date fechaCreado) {
        this.fechaCreado = fechaCreado;
    }

    public Date getFechaTerminado() {
        return fechaTerminado;
    }

    public void setFechaTerminado(Date fechaTerminado) {
        this.fechaTerminado = fechaTerminado;
    }

    public Point getCentroide() {
        if (poligono == null) return null;
        return poligono.getCentroid();
    }

    public boolean checkMaxMiembros() {
        return miembros.size() <= tipoProyecto.getMaxMiembros();
    }

    public int getCantMiembros() {
        return miembros.size();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proyecto proyecto = (Proyecto) o;
        if (id == null || proyecto.id == null) return false;
        return Objects.equals(id, proyecto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
