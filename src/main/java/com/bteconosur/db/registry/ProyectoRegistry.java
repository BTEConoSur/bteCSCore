package com.bteconosur.db.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.db.model.Proyecto;

public class ProyectoRegistry extends Registry<String, Proyecto> {

    private static ProyectoRegistry instance;

    public ProyectoRegistry() {
        super(Proyecto.class);
        logger.info(lang.getString("proyecto-registry-initializing"));  
        loadedObjects = new ConcurrentHashMap<>();
        List<Proyecto> proyectos = dbManager.selectAll(Proyecto.class);
        if (proyectos != null) for (Proyecto p : proyectos) loadedObjects.put(p.getId(), p);
    }

    @Override
    public void load(Proyecto obj) {
        if (obj == null || obj.getId() == null) return;
        loadedObjects.put(obj.getId(), obj);
        dbManager.save(obj);
    }

    public void shutdown() {
        logger.info(lang.getString("proyecto-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public static ProyectoRegistry getInstance() {
        if (instance == null) {
            instance = new ProyectoRegistry();
        }
        return instance;
    }

}
