package com.bteconosur.db.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.TipoProyecto;

/**
 * Registro de tipos de proyecto.
 */
public class TipoProyectoRegistry extends Registry<Long, TipoProyecto>{

    private static TipoProyectoRegistry instance;

    /**
     * Inicializa el registro y carga los tipos de proyecto persistidos.
     */
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

    /**
     * Carga un tipo de proyecto en persistencia y memoria.
     *
     * @param obj tipo de proyecto a cargar.
     */
    @Override
    public void load(TipoProyecto obj) {
        if (obj == null || obj.getNombre() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
    }

    /**
     * Obtiene el tipo de proyecto correspondiente a un tamaño.
     *
     * @param size tamaño del proyecto.
     * @return tipo de proyecto que contiene ese rango, o {@code null}.
     */
    public TipoProyecto get(Double size) {
        for (TipoProyecto tipo : loadedObjects.values()) {
            if (tipo.getTamanoMin() == null || tipo.getTamanoMax() == null) continue;
            if (tipo.getTamanoMin() <= size && tipo.getTamanoMax() >= size) {
                return tipo;
            }
        }
        return null;
    }

    /**
     * Cierra el registro y limpia su cache en memoria.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("tipo-proyecto-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    /**
     * Obtiene la instancia singleton de {@code TipoProyectoRegistry}.
     *
     * @return instancia única del registro.
     */
    public static TipoProyectoRegistry getInstance() {
        if (instance == null) {
            instance = new TipoProyectoRegistry();
        }
        return instance;
    }

}
