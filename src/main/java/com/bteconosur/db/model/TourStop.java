package com.bteconosur.db.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "tour_stop")
public class TourStop {

    @EmbeddedId
    private TourStopId id;

    @ManyToOne
    @MapsId("tourId")
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
        this.id = new TourStopId(tour.getId(), id);
        this.tour = tour;
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

    public TourStopId getId() {
        return id;
    }

    public void setId(TourStopId id) {
        this.id = id;
    }

    public String getTourStopId() {
        return id.getTourstopId();
    }

    public String getTourId() {
        return id.getTourId();
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
        String key = "tours." + tour.getId() + ".stops." + this.id + ".desc";
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

    @Embeddable
    /**
     * Clave compuesta de un tour stop (tour + id).
     */
    public static class TourStopId implements Serializable {
        
        @Column(name = "tour_id", length = 30, nullable = false)
        @JdbcTypeCode(SqlTypes.CHAR)
        private String tourId;

        @Column(name = "tour_stop_id", length = 30, nullable = false)
        @JdbcTypeCode(SqlTypes.CHAR)
        private String tourstopId;

        public TourStopId() {
        }

        public TourStopId(String tourId, String tourstopId) {
            this.tourId = tourId;
            this.tourstopId = tourstopId;
        }

        public String getTourId() {
            return tourId;
        }

        public void setTourId(String tourId) {
            this.tourId = tourId;
        }

        public String getTourstopId() {
            return tourstopId;
        }

        public void setTourstopId(String tourstopId) {
            this.tourstopId = tourstopId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TourStopId tourStopId = (TourStopId) o;
            return tourId.equals(tourStopId.tourId) && tourstopId.equals(tourStopId.tourstopId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tourId, tourstopId);
        }
    }

}
