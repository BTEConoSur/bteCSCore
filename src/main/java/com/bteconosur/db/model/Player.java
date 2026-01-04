package com.bteconosur.db.model;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

import org.bukkit.Bukkit;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bteconosur.db.registry.PlayerRegistry;

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

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    private Configuration configuration;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Pwarp> pwarps = new ArrayList<>();

    @OneToMany(mappedBy = "lider")
    @JsonIgnore
    private Set<Proyecto> proyectosLiderados = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "proyecto_miembro", joinColumns = @JoinColumn(name = "uuid_player"), inverseJoinColumns = @JoinColumn(name = "id_proyecto"))
    @JsonIgnore
    private Set<Proyecto> proyectos = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "pais_manager", joinColumns = @JoinColumn(name = "uuid_player"), inverseJoinColumns = @JoinColumn(name = "id_pais"))
    @JsonIgnore
    private Set<Pais> paisesManager = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "pais_reviewer", joinColumns = @JoinColumn(name = "uuid_player"), inverseJoinColumns = @JoinColumn(name = "id_pais"))
    @JsonIgnore //TODO: ver estos casos
    private Set<Pais> paisesReviewer = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "id_pais_prefix")
    private Pais paisPrefix;

    public Player() {
    }

    public Player(UUID uuid, String nombre, Date fechaIngreso, TipoUsuario tipoUsuario, RangoUsuario rangoUsuario) {
        this.uuid = uuid;
        this.nombre = nombre;
        this.nombrePublico = nombre;
        this.fechaIngreso = fechaIngreso;
        this.fechaUltimaConexion = new Date();
        this.tipoUsuario = tipoUsuario;
        this.rangoUsuario = rangoUsuario;
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

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @JsonIgnore
    public org.bukkit.entity.Player getBukkitPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    @JsonIgnore
    public static Player getBTECSPlayer(org.bukkit.entity.Player bukkitPlayer) {
        return PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId());
    }

    @JsonIgnore
    public static List<Player> getOnlinePlayers() {
        PlayerRegistry registry = PlayerRegistry.getInstance();
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(player -> registry.get(player.getUniqueId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(uuid, player.uuid);
    }

    @JsonIgnore
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    //TODO: Ver cascada
}
