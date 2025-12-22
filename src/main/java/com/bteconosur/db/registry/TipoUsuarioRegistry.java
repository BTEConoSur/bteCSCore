package com.bteconosur.db.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.db.model.TipoUsuario;

public class TipoUsuarioRegistry extends Registry<String, TipoUsuario> {

    private static TipoUsuarioRegistry instance;

    public TipoUsuarioRegistry() {
        super(TipoUsuario.class);
        logger.info(lang.getString("tipo-usuario-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<TipoUsuario> tipos = dbManager.selectAll(TipoUsuario.class);
        if (tipos != null) {
            for (TipoUsuario tipo : tipos) {
                if (tipo.getNombre() != null) {
                    loadedObjects.put(tipo.getNombre(), tipo);
                }
            }
        }
        ensureDefaults();
    }

    @Override
    public void load(TipoUsuario obj) {
        if (obj == null || obj.getNombre() == null) return;
        loadedObjects.put(obj.getNombre(), obj);
        dbManager.save(obj);
    }

    public TipoUsuario getVisita() {
        return get("Visita");
    }

    public TipoUsuario getPostulante() {
        return get("Postulante");
    }

    public TipoUsuario getConstructor() {
        return get("Constructor");
    }

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

    public void shutdown() {
        logger.info(lang.getString("tipo-usuario-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public static TipoUsuarioRegistry getInstance() {
        if (instance == null) {
            instance = new TipoUsuarioRegistry();
        }
        return instance;
    }

}
