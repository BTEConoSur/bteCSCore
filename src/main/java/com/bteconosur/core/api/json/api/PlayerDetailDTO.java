package com.bteconosur.core.api.json.api;

import java.util.Date;
import java.util.UUID;

import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;

public class PlayerDetailDTO {

    private UUID id;
    private String nombre;
    private String nombrePublico;
    private Long dsIdUsuario;
    private Long fechaIngreso;
    private Long fechaUltimaConexion;
    private RangoUsuario rangoUsuario;
    private TipoUsuario tipoUsuario;
    private PaisSummaryDTO paisPrefix;

    public PlayerDetailDTO() {
    }

    public PlayerDetailDTO(Player p) {
        this.id = p.getUuid();
        this.nombre = p.getNombre();
        this.nombrePublico = p.getNombrePublico();
        this.dsIdUsuario = p.getDsIdUsuario();
        this.fechaIngreso = p.getFechaIngreso() != null ? p.getFechaIngreso().getTime() : null;
        this.fechaUltimaConexion = p.getFechaUltimaConexion() != null ? p.getFechaUltimaConexion().getTime() : null;
        this.rangoUsuario = p.getRangoUsuario();
        this.tipoUsuario = p.getTipoUsuario();
        if (p.getPaisPrefix() != null) {
            this.paisPrefix = new PaisSummaryDTO(p.getPaisPrefix());
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Long getDsIdUsuario() {
        return dsIdUsuario;
    }

    public void setDsIdUsuario(Long dsIdUsuario) {
        this.dsIdUsuario = dsIdUsuario;
    }

    public Long getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Long fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Long getFechaUltimaConexion() {
        return fechaUltimaConexion;
    }

    public void setFechaUltimaConexion(Long fechaUltimaConexion) {
        this.fechaUltimaConexion = fechaUltimaConexion;
    }

    public RangoUsuario getRangoUsuario() {
        return rangoUsuario;
    }

    public void setRangoUsuario(RangoUsuario rangoUsuario) {
        this.rangoUsuario = rangoUsuario;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public PaisSummaryDTO getPaisPrefix() {
        return paisPrefix;
    }

    public void setPaisPrefix(PaisSummaryDTO paisPrefix) {
        this.paisPrefix = paisPrefix;
    }

}
