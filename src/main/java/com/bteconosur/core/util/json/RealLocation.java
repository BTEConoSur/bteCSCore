package com.bteconosur.core.util.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo de datos para ubicación geolocalización deserializada desde JSON.
 * Representa un lugar real con coordenadas GPS y nombre para mostrar.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealLocation {

    /**
     * Latitud geográfica del lugar.
     */
    public double lat;
    
    /**
     * Longitud geográfica del lugar.
     */
    public double lon;

    /**
     * Nombre para mostrar del lugar.
     */
    @JsonProperty("display_name")
    public String displayName;

}
