package com.bteconosur.core.util;

import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;

public class TerraUtils {

    private static final GeographicProjection geographicProjection = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection();

    public static double[] toMc(double lat, double lon) {
        try {
            //ConsoleLogger.debug("Convirtiendo coordenadas geográficas a Minecraft: Lat=" + lat + ", Lon=" + lon);   
            return geographicProjection.fromGeo(lon, lat);
        } catch (Exception e) {
            ConsoleLogger.error("Error al convertir coordenadas geográficas a Minecraft: ", e);
            return null;
        }
    }

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