package com.bteconosur.db.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.RangoUsuario;

public class RangoUsuarioRegistry extends Registry<Long, RangoUsuario> {

    private static RangoUsuarioRegistry instance;

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

    @Override
    public void load(RangoUsuario obj) {
        if (obj == null || obj.getId() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
    }

    public RangoUsuario get(String name) {
        for (RangoUsuario rango : loadedObjects.values()) {
            if (rango.getNombre().equalsIgnoreCase(name)) {
                return rango;
            }
        }
        return null;
    }

    public RangoUsuario getAdmin() {
        return get("Admin");
    }

    public RangoUsuario getNormal() {
        return get("Normal");
    }

    public RangoUsuario getMod() {
        return get("Mod");
    }

    public RangoUsuario getInfluencer() {
        return get("Influencer");
    }

    public RangoUsuario getDonador() {
        return get("Donador");
    }

    private void ensureDefaults() {
        if (get("Normal") == null) {
            RangoUsuario rango = new RangoUsuario("Normal", "Rango de usuario: Normal");
            load(rango);
        }
        if (get("Admin") == null) {
            RangoUsuario rango = new RangoUsuario("Admin", "Rango de usuario: Admin");
            load(rango);
        }
        if (get("Mod") == null) {
            RangoUsuario rango = new RangoUsuario("Mod", "Rango de usuario: Mod");
            load(rango);
        }
        if (get("Influencer") == null) {
            RangoUsuario rango = new RangoUsuario("Influencer", "Rango de usuario: Influencer");
            load(rango);
        }
        if (get("Donador") == null) {
            RangoUsuario rango = new RangoUsuario("Donador", "Rango de usuario: Donador");
            load(rango);
        }
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("rango-usuario-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public static RangoUsuarioRegistry getInstance() {
        if (instance == null) {
            instance = new RangoUsuarioRegistry();
        }
        return instance;
    }

}
