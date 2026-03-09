package com.bteconosur.db.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.RangoUsuario;

/**
 * Registro de rangos de usuario.
 */
public class RangoUsuarioRegistry extends Registry<Long, RangoUsuario> {

    private static RangoUsuarioRegistry instance;

    /**
     * Inicializa el registro, carga rangos persistidos y asegura valores por defecto.
     */
    public RangoUsuarioRegistry() {
        super();
        ConsoleLogger.info(LanguageHandler.getText("rango-usuario-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<RangoUsuario> rangos = dbManager.selectAll(RangoUsuario.class);
        if (rangos != null) {
            for (RangoUsuario rango : rangos) {
                if (rango.getId() != null) {
                    loadedObjects.put(rango.getId(), rango);
                }
            }
        }
        ensureDefaults();
    }

    /**
     * Carga un rango de usuario en persistencia y memoria.
     *
     * @param obj rango de usuario a cargar.
     */
    @Override
    public void load(RangoUsuario obj) {
        if (obj == null || obj.getId() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
    }

    /**
     * Obtiene un rango de usuario por nombre.
     *
     * @param name nombre del rango.
     * @return rango encontrado, o {@code null}.
     */
    public RangoUsuario get(String name) {
        for (RangoUsuario rango : loadedObjects.values()) {
            if (rango.getNombre().equalsIgnoreCase(name)) {
                return rango;
            }
        }
        return null;
    }

    /** @return rango Admin. */
    public RangoUsuario getAdmin() {
        return get("Admin");
    }

    /** @return rango Normal. */
    public RangoUsuario getNormal() {
        return get("Normal");
    }

    /** @return rango Mod. */
    public RangoUsuario getMod() {
        return get("Mod");
    }

    /** @return rango Influencer. */
    public RangoUsuario getInfluencer() {
        return get("Influencer");
    }

    /** @return rango Donador. */
    public RangoUsuario getDonador() {
        return get("Donador");
    }

    /**
     * Asegura la existencia de rangos de usuario base.
     */
    private void ensureDefaults() {
        if (get("Normal") == null) {
            RangoUsuario rango = new RangoUsuario("Normal");
            load(rango);
        }
        if (get("Admin") == null) {
            RangoUsuario rango = new RangoUsuario("Admin");
            load(rango);
        }
        if (get("Mod") == null) {
            RangoUsuario rango = new RangoUsuario("Mod");
            load(rango);
        }
        if (get("Influencer") == null) {
            RangoUsuario rango = new RangoUsuario("Influencer");
            load(rango);
        }
        if (get("Donador") == null) {
            RangoUsuario rango = new RangoUsuario("Donador");
            load(rango);
        }
    }

    /**
     * Cierra el registro y limpia su cache en memoria.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("rango-usuario-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    /**
     * Obtiene la instancia singleton de {@code RangoUsuarioRegistry}.
     *
     * @return instancia única del registro.
     */
    public static RangoUsuarioRegistry getInstance() {
        if (instance == null) {
            instance = new RangoUsuarioRegistry();
        }
        return instance;
    }

}
