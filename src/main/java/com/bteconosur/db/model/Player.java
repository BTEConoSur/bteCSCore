package com.bteconosur.db.model;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Table(name = "player")
public class Player {

    @Id
    @Column(name = "uuid", length = 36) // Define la columna y su longitud
    private UUID uuid;

    @Column(name = "nombre", length = 16) // Define la columna y su longitud
    private String nombre;

    @Column(name = "f_ingreso")
    private Date fechaIngreso;

    public Player() {
    }

    public Player(UUID uuid, String nombre, Date fechaIngreso) {
        this.uuid = uuid;
        this.nombre = nombre;
        this.fechaIngreso = fechaIngreso;
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

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }


}
