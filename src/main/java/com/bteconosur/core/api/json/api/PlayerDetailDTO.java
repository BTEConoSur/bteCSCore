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
    private Date fechaIngreso;
    private Date fechaUltimaConexion;
    private RangoUsuario rangoUsuario;
    private TipoUsuario tipoUsuario;
    private PaisSummaryDTO paisPrefix;

    public PlayerDetailDTO(Player p) {
        this.id = p.getUuid();
        this.nombre = p.getNombre();
        this.nombrePublico = p.getNombrePublico();
        this.dsIdUsuario = p.getDsIdUsuario();
        this.fechaIngreso = p.getFechaIngreso();
        this.fechaUltimaConexion = p.getFechaUltimaConexion();
        this.rangoUsuario = p.getRangoUsuario();
        this.tipoUsuario = p.getTipoUsuario();
        if (p.getPaisPrefix() != null) {
            this.paisPrefix = new PaisSummaryDTO(p.getPaisPrefix());
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombrePublico() {
        return nombrePublico;
    }

    public Long getDsIdUsuario() {
        return dsIdUsuario;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public Date getFechaUltimaConexion() {
        return fechaUltimaConexion;
    }

    public RangoUsuario getRangoUsuario() {
        return rangoUsuario;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public PaisSummaryDTO getPaisPrefix() {
        return paisPrefix;
    }

}
