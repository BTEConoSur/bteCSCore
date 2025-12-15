package com.bteconosur.db.model;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import org.bukkit.Bukkit;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bteconosur.db.DBManager;

@Entity
@Table(name = "player")
public class Player {

    @Id
    @Column(name = "uuid", columnDefinition = "CHAR(36)", nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID uuid;

    @Column(name = "nombre", length = 16) // Define la columna y su longitud
    private String nombre;

    @Column(name = "f_ingreso")
    private Date fechaIngreso;

    @ManyToOne
    @JoinColumn(name = "id_tipo_usuario")
    private TipoUsuario tipoUsuario;

    public Player() {
    }

    public Player(UUID uuid, String nombre, Date fechaIngreso, TipoUsuario tipoUsuario) {
        this.uuid = uuid;
        this.nombre = nombre;
        this.fechaIngreso = fechaIngreso;
        this.tipoUsuario = tipoUsuario;
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

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public org.bukkit.entity.Player getBukkitPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public static Player getBTECSPlayer(org.bukkit.entity.Player bukkitPlayer) {
        return DBManager.getInstance().get(Player.class, bukkitPlayer.getUniqueId()); //TODO Ver si es mejor cachear
    }

    //TODO: Ver cascada
}
