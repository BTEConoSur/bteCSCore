package com.bteconosur.db.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "region_division")
public class RegionDivision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_region", nullable = false)
    private Long id;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "poligono")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Polygon poligono;

    @Transient
    private PreparedGeometry preparedGeometry;
    @Transient
    private Envelope boundingBox;

    @ManyToOne
    @JoinColumn(name = "id_division")
    private Division division;

    public RegionDivision() {
    }

    public RegionDivision(Division division, String nombre, Polygon poligono) {
        this.nombre = nombre;
        this.poligono = poligono;
        this.division = division;
        this.preparedGeometry = PreparedGeometryFactory.prepare(poligono);
        this.boundingBox = poligono.getEnvelopeInternal();
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

    public Polygon getPoligono() {
        return poligono;
    }

    public void setPoligono(Polygon poligono) {
        this.poligono = poligono;
        this.preparedGeometry = PreparedGeometryFactory.prepare(poligono);
        this.boundingBox = poligono.getEnvelopeInternal();
    }

    public Envelope getBoundingBox() {
        return boundingBox;
    }

    public PreparedGeometry getPreparedGeometry() {
        return preparedGeometry;
    }   

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division    ;
    }

    @PostLoad
    private void initTransientFields() {
        if (poligono != null) {
            this.preparedGeometry = PreparedGeometryFactory.prepare(poligono);
            this.boundingBox = poligono.getEnvelopeInternal();
        }
    }

}
