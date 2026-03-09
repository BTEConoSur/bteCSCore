package com.bteconosur.world.model;

/**
 * Representa la capa alta del mundo BTE.
 * Extiende LabelWorld para proporcionar funcionalidad específica de capa.
 */
public class CapaAlta extends LabelWorld {

    /**
     * Constructor de la capa alta.
     * 
     * @param name Nombre del mundo de Bukkit
     * @param displayName Nombre para mostrar al jugador
     * @param offset Offset vertical de la capa
     */
    public CapaAlta(String name, String displayName, int offset) {
        super(name, displayName, offset);
    }
    
}
