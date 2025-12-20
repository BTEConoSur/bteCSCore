package com.bteconosur.db.registry;

import java.util.HashMap;
import java.util.List;

import com.bteconosur.db.model.RangoUsuario;

public class RangoUsuarioRegistry extends Registry<String, RangoUsuario> {

    private static RangoUsuarioRegistry instance;

    public RangoUsuarioRegistry() {
        super(RangoUsuario.class);
        logger.info(lang.getString("rango-usuario-registry-initializing"));
        loadedObjects = new HashMap<>();
        List<RangoUsuario> rangos = dbManager.selectAll(RangoUsuario.class);
        if (rangos != null) {
            for (RangoUsuario rango : rangos) {
                if (rango.getNombre() != null) {
                    loadedObjects.put(rango.getNombre(), rango);
                }
            }
        }
        ensureDefaults();
    }

    @Override
    public void load(RangoUsuario obj) {
        if (obj == null || obj.getNombre() == null) return;
        loadedObjects.put(obj.getNombre(), obj);
        dbManager.save(obj);
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

    public RangoUsuario getReviewer() {
        return get("Reviewer");
    }

    public RangoUsuario getInfluencer() {
        return get("Influencer");
    }

    public RangoUsuario getDonador() {
        return get("Donador");
    }

    private void ensureDefaults() {
        if (get("Normal") == null) {
            RangoUsuario rango = new RangoUsuario();
            rango.setNombre("Normal");
            rango.setDescripcion("Rango de usuario: Normal");
            load(rango);
        }
        if (get("Admin") == null) {
            RangoUsuario rango = new RangoUsuario();
            rango.setNombre("Admin");
            rango.setDescripcion("Rango de usuario: Admin");
            load(rango);
        }
        if (get("Mod") == null) {
            RangoUsuario rango = new RangoUsuario();
            rango.setNombre("Mod");
            rango.setDescripcion("Rango de usuario: Mod");
            load(rango);
        }
        if (get("Reviewer") == null) {
            RangoUsuario rango = new RangoUsuario();
            rango.setNombre("Reviewer");
            rango.setDescripcion("Rango de usuario: Reviewer");
            load(rango);
        }
        if (get("Influencer") == null) {
            RangoUsuario rango = new RangoUsuario();
            rango.setNombre("Influencer");
            rango.setDescripcion("Rango de usuario: Influencer");
            load(rango);
        }
        if (get("Donador") == null) {
            RangoUsuario rango = new RangoUsuario();
            rango.setNombre("Donador");
            rango.setDescripcion("Rango de usuario: Donador");
            load(rango);
        }
    }

    public void shutdown() {
        logger.info(lang.getString("rango-usuario-registry-shutting-down"));
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
