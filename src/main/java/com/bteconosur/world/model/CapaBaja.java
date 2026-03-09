package com.bteconosur.world.model;

/**
 * Representa la capa baja del mundo BTE.
 * Extiende LabelWorld para proporcionar funcionalidad específica de capa.
 */
public class CapaBaja extends LabelWorld {

    /**
     * Constructor de la capa baja.
     * 
     * @param name Nombre del mundo de Bukkit
     * @param displayName Nombre para mostrar al jugador
     * @param offset Offset vertical de la capa
     */
    public CapaBaja(String name, String displayName, int offset) {
        super(name, displayName, offset);
    }

}
