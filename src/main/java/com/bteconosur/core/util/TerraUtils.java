package com.bteconosur.core.util;

import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;

/**
 * Utilidad para conversión de coordenadas entre geografía y Minecraft.
 * Proporciona transformación bidireccional entre coordenadas GPS (latitud/longitud)
 * y bloques de Minecraft (X, Z) usando la proyección de Build the Earth.
 */
public class TerraUtils {

    private static final GeographicProjection geographicProjection = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection();

    /**
     * Convierte coordenadas geográficas (latitud/longitud) a coordenadas de Minecraft.
     *
     * @param lat latitud geográfica.
     * @param lon longitud geográfica.
     * @return arreglo con coordenadas Minecraft {@code [X, Z]} o {@code null} si hay error.
     */
    public static double[] toMc(double lat, double lon) {
        try {
            //ConsoleLogger.debug("Convirtiendo coordenadas geográficas a Minecraft: Lat=" + lat + ", Lon=" + lon);   
            return geographicProjection.fromGeo(lon, lat);
        } catch (Exception e) {
            ConsoleLogger.error("Error al convertir coordenadas geográficas a Minecraft: ", e);
            return null;
        }
    }

    /**
     * Convierte coordenadas de Minecraft (X, Z) a coordenadas geográficas.
     *
     * @param x coordenada X en Minecraft.
     * @param z coordenada Z en Minecraft.
     * @return arreglo con coordenadas geográficas {@code [latitud, longitud]} o {@code null} si hay error.
     */
    public static double[] toGeo(double x, double z) {
        try {
            //ConsoleLogger.debug("Convirtiendo coordenadas de Minecraft a geográficas: X=" + x + ", Z=" + z);    
            return geographicProjection.toGeo(x, z);
        } catch (Exception e) {
            ConsoleLogger.error("Error al convertir coordenadas de Minecraft a geográficas: ", e);
            return null;
        }
    }

}
//Maps -> Lat Lon
//GeoJson -> Lon Lat