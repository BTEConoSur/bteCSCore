package com.bteconosur.db.model;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

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

    @Column(name = "nombre", length = 16, nullable = false)
    private String nombre;

    @Column(name = "nombre_publico", length = 16, nullable = false)
    private String nombrePublico;

    @Column(name = "f_ingreso", nullable = false)
    private Date fechaIngreso;

    @Column(name = "f_ult_conexion", nullable = false)
    private Date fechaUltimaConexion;

    @Column(name = "ds_id_usuario")
    private Long dsIdUsuario;

    @ManyToOne
    @JoinColumn(name = "id_tipo_usuario")
    private TipoUsuario tipoUsuario;

    @ManyToOne
    @JoinColumn(name = "id_rango_usuario")
    private RangoUsuario rangoUsuario;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pwarp> pwarps = new ArrayList<>();

    @OneToMany(mappedBy = "lider")
    private Set<Proyecto> proyectosLiderados = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "proyecto_miembro", joinColumns = @JoinColumn(name = "uuid_player"), inverseJoinColumns = @JoinColumn(name = "id_proyecto"))
    private Set<Proyecto> proyectos = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "pais_manager", joinColumns = @JoinColumn(name = "uuid_player"), inverseJoinColumns = @JoinColumn(name = "id_pais"))
    private Set<Pais> paisesManager = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "pais_reviewer", joinColumns = @JoinColumn(name = "uuid_player"), inverseJoinColumns = @JoinColumn(name = "id_pais"))
    private Set<Pais> paisesReviewer = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "id_pais_prefix")
    private Pais paisPrefix;

    public Player() {
    }

    public Player(UUID uuid, String nombre, Date fechaUltimaConexion, TipoUsuario tipoUsuario) {
        this.uuid = uuid;
        this.nombre = nombre;
        this.fechaUltimaConexion = fechaUltimaConexion;
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

    public String getNombrePublico() {
        return nombrePublico;
    }

    public void setNombrePublico(String nombrePublico) {
        this.nombrePublico = nombrePublico;
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

    public RangoUsuario getRangoUsuario() {
        return rangoUsuario;
    }

    public void setRangoUsuario(RangoUsuario rangoUsuario) {
        this.rangoUsuario = rangoUsuario;
    }

    public Date getFechaUltimaConexion() {
        return fechaUltimaConexion;
    }

    public void setFechaUltimaConexion(Date fechaUltimaConexion) {
        this.fechaUltimaConexion = fechaUltimaConexion;
    }

    public Long getDsIdUsuario() {
        return dsIdUsuario;
    }

    public void setDsIdUsuario(Long dsIdUsuario) {
        this.dsIdUsuario = dsIdUsuario;
    }

    public List<Pwarp> getPwarps() {
        return pwarps;
    }

    public void setPwarps(List<Pwarp> pwarps) {
        this.pwarps = pwarps;
    }

    public Set<Proyecto> getProyectosLiderados() {
        return proyectosLiderados;
    }

    public void setProyectosLiderados(Set<Proyecto> proyectosLiderados) {
        this.proyectosLiderados = proyectosLiderados;
    }

    public Set<Proyecto> getProyectos() {
        return proyectos;
    }

    public void setProyectos(Set<Proyecto> proyectos) {
        this.proyectos = proyectos;
    }

    public Set<Pais> getPaisesManager() {
        return paisesManager;
    }

    public void setPaisesManager(Set<Pais> paisesManager) {
        this.paisesManager = paisesManager;
    }

    public Set<Pais> getPaisesReviewer() {
        return paisesReviewer;
    }

    public void setPaisesReviewer(Set<Pais> paisesReviewer) {
        this.paisesReviewer = paisesReviewer;
    }

    public Pais getPaisPrefix() {
        return paisPrefix;
    }

    public void setPaisPrefix(Pais paisPrefix) {
        this.paisPrefix = paisPrefix;
    }

    public org.bukkit.entity.Player getBukkitPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public static Player getBTECSPlayer(org.bukkit.entity.Player bukkitPlayer) {
        return DBManager.getInstance().get(Player.class, bukkitPlayer.getUniqueId()); //TODO Ver si es mejor cachear
    }

    //TODO: Ver cascada
}
