package com.bteconosur.db.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.TipoProyecto;

public class TipoProyectoRegistry extends Registry<Long, TipoProyecto>{

    private static TipoProyectoRegistry instance;

    public TipoProyectoRegistry() {
        super();
        ConsoleLogger.info(LanguageHandler.getText("tipo-proyecto-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<TipoProyecto> tipos = dbManager.selectAll(TipoProyecto.class);
        if (tipos != null) {
            for (TipoProyecto tipo : tipos) {
                if (tipo.getNombre() != null) {
                    loadedObjects.put(tipo.getId(), tipo);
                }
            }
        }
    }

    @Override
    public void load(TipoProyecto obj) {
        if (obj == null || obj.getNombre() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
    }

    public TipoProyecto get(Double size) {
        for (TipoProyecto tipo : loadedObjects.values()) {
            if (tipo.getTamanoMin() <= size && tipo.getTamanoMax() >= size) {
                return tipo;
            }
        }
        return null;
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("tipo-proyecto-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public static TipoProyectoRegistry getInstance() {
        if (instance == null) {
            instance = new TipoProyectoRegistry();
        }
        return instance;
    }

}
