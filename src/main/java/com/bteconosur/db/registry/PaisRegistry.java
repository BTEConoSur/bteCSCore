package com.bteconosur.db.registry;

import java.util.HashMap;
import java.util.List;

import com.bteconosur.db.model.Pais;

public class PaisRegistry extends Registry<String, Pais> {

    private static PaisRegistry instance;

    public PaisRegistry() {
        super(Pais.class);
        logger.info(lang.getString("pais-registry-initializing"));
        loadedObjects = new HashMap<>();
        List<Pais> paises = dbManager.selectAll(Pais.class);
        if (paises != null) for (Pais p : paises) loadedObjects.put(p.getNombre().toLowerCase(), p);
    }

    @Override
    public void load(Pais obj) {
        if (obj == null || obj.getNombre() == null) return;
        loadedObjects.put(obj.getNombre().toLowerCase(), obj);
        dbManager.save(obj);
    }

    public void shutdown() {
        logger.info(lang.getString("pais-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public static PaisRegistry getInstance() {
        if (instance == null) {
            instance = new PaisRegistry();
        }
        return instance;
    }

}
