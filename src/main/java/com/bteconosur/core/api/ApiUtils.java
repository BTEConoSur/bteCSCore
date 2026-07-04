package com.bteconosur.core.api;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.TerraUtils;

public class ApiUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static Polygon toGeoPolygon(Polygon mcPolygon) {
        try {
            LinearRing exterior = transformRing(mcPolygon.getExteriorRing());

            int numInteriores = mcPolygon.getNumInteriorRing();
            LinearRing[] interiores = new LinearRing[numInteriores];
            for (int i = 0; i < numInteriores; i++) {
                interiores[i] = transformRing(mcPolygon.getInteriorRingN(i));
            }

            return geometryFactory.createPolygon(exterior, interiores);
        } catch (Exception e) {
            ConsoleLogger.error("Error al convertir polígono de Minecraft a coordenadas geográficas: ", e);
            return null;
        }
    }

    private static LinearRing transformRing(Geometry ring) {
        Coordinate[] original = ring.getCoordinates();
        Coordinate[] transformadas = new Coordinate[original.length];

        for (int i = 0; i < original.length; i++) {
            double x = original[i].x;
            double z = original[i].y;
            double[] geo = TerraUtils.toGeo(x, z);
            if (geo == null) {
                throw new IllegalStateException("No se pudo convertir la coordenada (" + x + ", " + z + ")");
            }

            double lat = geo[0];
            double lon = geo[1];
            // GeoJSON exige orden [longitud, latitud]
            transformadas[i] = new Coordinate(lon, lat);
        }

        return geometryFactory.createLinearRing(transformadas);
    }

}
