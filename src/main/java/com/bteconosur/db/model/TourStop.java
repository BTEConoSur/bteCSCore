package com.bteconosur.db.model;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "tour_stop")
public class TourStop {

    @Id
    @Column(name = "id", length = 30)
    private String id;

    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(nullable = false) private String worldName;
    @Column(nullable = false) private double x;
    @Column(nullable = false) private double y;
    @Column(nullable = false) private double z;
    @Column(nullable = false) private float yaw;
    @Column(nullable = false) private float pitch;

    @Column(name = "poligono")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Polygon poligono;

    @Transient
    private PreparedGeometry preparedGeometry;
    @Transient
    private Envelope boundingBox;

    public TourStop() {}

    public TourStop(Tour tour, String id, int orden, Location location, Polygon poligono) {
        this.tour = tour;
        this.id = id;
        this.orden = orden;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.worldName = location.getWorld().getName();
        this.poligono = poligono;
        this.preparedGeometry = PreparedGeometryFactory.prepare(poligono);
        this.boundingBox = poligono.getEnvelopeInternal();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public Location getLocation() {
        return new Location(
            Bukkit.getWorld(worldName),
            x, y, z,
            yaw, pitch
        );
    }

    public Polygon getPoligono() {
        return poligono;
    }

    public void setPoligono(Polygon poligono) {
        this.poligono = poligono;
    }

    public PreparedGeometry getPreparedGeometry() {
        return preparedGeometry;
    }

    public Envelope getBoundingBox() {
        return boundingBox;
    }

    public Point getCentroide() {
        if (poligono == null) return null;
        return poligono.getCentroid();
    }

    public void setLocation(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.worldName = location.getWorld().getName();
    }

    public List<String> getDescription(Language language) {
        String key = "tour." + tour.getId() + ".stops." + this.orden + ".desc";
        List<String> translated = LanguageHandler.getTextList(language, key);
        if (!translated.isEmpty()) return translated;
        return LanguageHandler.getTextList(Language.getDefault(), key);
    }

    @PostLoad
    public void initTransientFields() {
        if (poligono != null) {
            this.preparedGeometry = PreparedGeometryFactory.prepare(poligono);
            this.boundingBox = poligono.getEnvelopeInternal();
        }
    }

}
