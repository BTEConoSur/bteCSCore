package com.bteconosur.db.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_usuario")
public class TipoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id_tipo_usuario")
    private Long id;

    @Column(name = "nombre", length = 20)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "cant_proyec_sim")
    private Integer cantProyecSim; // "Cantidad de proyectos simultáneos"

    @OneToMany(mappedBy = "tipoUsuario")
    private Set<Player> players = new HashSet<>();
    
    public TipoUsuario() {
    }

    public TipoUsuario(String nombre, String descripcion, Integer cantProyecSim) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantProyecSim = cantProyecSim;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCantProyecSim() {
        return cantProyecSim;
    }

    public void setCantProyecSim(Integer cantProyecSim) {
        this.cantProyecSim = cantProyecSim;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Player> players) {
        this.players = players;
    }

}
