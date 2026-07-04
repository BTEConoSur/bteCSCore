package com.bteconosur.core.api.json.api;

import java.util.UUID;

import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;

public class PlayerSummaryDTO {

    private UUID id;
    private String nombre;
    private String nombrePublico;
    private RangoUsuario rangoUsuario;
    private TipoUsuario tipoUsuario;
    private PaisSummaryDTO paisPrefix;

    public PlayerSummaryDTO(Player p) {
        this.id = p.getUuid();
        this.nombre = p.getNombre();
        this.nombrePublico = p.getNombrePublico();
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
