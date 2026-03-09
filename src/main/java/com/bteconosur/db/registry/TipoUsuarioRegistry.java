package com.bteconosur.db.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.TipoUsuario;

/**
 * Registro de tipos de usuario.
 */
public class TipoUsuarioRegistry extends Registry<Long, TipoUsuario> {

    private static TipoUsuarioRegistry instance;

    /**
     * Inicializa el registro, carga tipos persistidos y asegura valores por defecto.
     */
    public TipoUsuarioRegistry() {
        super();
        ConsoleLogger.info(LanguageHandler.getText("tipo-usuario-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<TipoUsuario> tipos = dbManager.selectAll(TipoUsuario.class);
        if (tipos != null) {
            for (TipoUsuario tipo : tipos) {
                if (tipo.getId() != null) {
                    loadedObjects.put(tipo.getId(), tipo);
                }
            }
        }
        ensureDefaults();
    }

    /**
     * Carga un tipo de usuario en persistencia y memoria.
     *
     * @param obj tipo de usuario a cargar.
     */
    @Override
    public void load(TipoUsuario obj) {
        if (obj == null || obj.getId() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
    }

    /**
     * Obtiene un tipo de usuario por nombre.
     *
     * @param name nombre del tipo.
     * @return tipo encontrado, o {@code null}.
     */
    public TipoUsuario get(String name) {
        for (TipoUsuario tipo : loadedObjects.values()) {
            if (tipo.getNombre().equalsIgnoreCase(name)) {
                return tipo;
            }
        }
        return null;
    }

    /** @return tipo de usuario Visita. */
    public TipoUsuario getVisita() {
        return get("Visita");
    }

    /** @return tipo de usuario Postulante. */
    public TipoUsuario getPostulante() {
        return get("Postulante");
    }

    /** @return tipo de usuario Constructor. */
    public TipoUsuario getConstructor() {
        return get("Constructor");
    }

    /**
     * Asegura la existencia de tipos de usuario base.
     */
    private void ensureDefaults() {
        if (get("Visita") == null) {
            TipoUsuario tipo = new TipoUsuario("Visita", "Tipo de usuario: Visita", 5);
            load(tipo);
        }
        if (get("Postulante") == null) {
            TipoUsuario tipo = new TipoUsuario("Postulante", "Tipo de usuario: Postulante", 10);
            load(tipo);
        }
        if (get("Constructor") == null) {
            TipoUsuario tipo = new TipoUsuario("Constructor", "Tipo de usuario: Constructor", 20);
            load(tipo);
        }
    }

    /**
     * Cierra el registro y limpia su cache en memoria.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("tipo-usuario-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    /**
     * Obtiene la instancia singleton de {@code TipoUsuarioRegistry}.
     *
     * @return instancia única del registro.
     */
    public static TipoUsuarioRegistry getInstance() {
        if (instance == null) {
            instance = new TipoUsuarioRegistry();
        }
        return instance;
    }

}
