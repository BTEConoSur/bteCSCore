package com.bteconosur.world.model;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

/**
 * Contenedor optimizado para datos de región geográfica.
 * Utiliza PreparedGeometry y Envelope para consultas espaciales rápidas.
 */
public class RegionData {

    private final Polygon polygon;
    private final PreparedGeometry prepared;
    private final Envelope envelope;

    /**
     * Constructor que prepara la geometría para consultas optimizadas.
     * 
     * @param polygon Polígono de la región
     */
    public RegionData(Polygon polygon) {
        this.prepared = PreparedGeometryFactory.prepare(polygon);
        this.envelope = polygon.getEnvelopeInternal();
        this.polygon = polygon;
    }

    /**
     * Obtiene la geometría preparada para verificaciones rápidas de contención.
     * 
     * @return PreparedGeometry optimizada
     */
    public PreparedGeometry getPrepared() {
        return prepared;
    }

    /**
     * Obtiene el envolvente (bounding box) del polígono para pre-filtraje rápido.
     * 
     * @return Envelope del polígono
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * Obtiene el polígono original de la región.
     * 
     * @return Polígono de la región
     */
    public Polygon getPolygon() {
        return polygon;
    }
}
