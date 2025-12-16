package com.bteconosur.db.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "pwarp")
public class Pwarp {

    @EmbeddedId
    private PwarpId id;

    @Column(name = "x", nullable = false)
    private Double x;

    @Column(name = "y", nullable = false)
    private Double y;

    @Column(name = "z", nullable = false)
    private Double z;

    @ManyToOne
    @MapsId("uuid")
    @JoinColumn(name = "uuid_player")
    private Player player;

    public Pwarp() {
    }

    public Pwarp(UUID uuid, String nombre, Player player, Double x, Double y, Double z) {
        this.id = new PwarpId(uuid, nombre);
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PwarpId getId() {
        return id;
    }

    public void setId(PwarpId id) {
        this.id = id;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getNombre() {
        return id.getNombre();
    }

    public UUID getUuidPlayer() {
        return id.getUuid();
    }

    @Embeddable
    public static class PwarpId implements Serializable {
        
        @Column(name = "uuid_player", columnDefinition = "CHAR(36)", nullable = false)
        @JdbcTypeCode(SqlTypes.CHAR)
        private UUID uuid;

        @Column(name = "nombre", length = 50, nullable = false)
        private String nombre;

        public PwarpId() {
        }

        public PwarpId(UUID uuid, String nombre) {
            this.uuid = uuid;
            this.nombre = nombre;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PwarpId pwarpId = (PwarpId) o;
            return uuid.equals(pwarpId.uuid) && nombre.equals(pwarpId.nombre);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, nombre);
        }
    }

}
