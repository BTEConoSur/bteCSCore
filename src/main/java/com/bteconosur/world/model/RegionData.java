package com.bteconosur.world.model;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

public class RegionData {

    private final Polygon polygon;
    private final PreparedGeometry prepared;
    private final Envelope envelope;

    public RegionData(Polygon polygon) {
        this.prepared = PreparedGeometryFactory.prepare(polygon);
        this.envelope = polygon.getEnvelopeInternal();
        this.polygon = polygon;
    }

    public PreparedGeometry getPrepared() {
        return prepared;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public Polygon getPolygon() {
        return polygon;
    }
}
