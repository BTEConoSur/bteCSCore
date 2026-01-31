package com.bteconosur.world.model;

import java.util.List;

import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.util.GeoJsonUtils;

public class CapaAlta extends LabelWorld {

    private final List<Polygon> regions;

    public CapaAlta(String name, String displayName, int offset) {
        super(name, displayName, offset);

        this.regions = loadRegions();
    }

    private List<Polygon> loadRegions() {
        return GeoJsonUtils.geoJsonToPolygons("world", getName() + ".geojson");
    }

    public List<Polygon> getRegions() {
        return this.regions;
    }
    
}
