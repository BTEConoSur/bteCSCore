package com.bteconosur.db.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.RegionDivision;
import com.bteconosur.db.model.RegionPais;

public class PaisRegistry extends Registry<String, Pais> {

    private static PaisRegistry instance;

    public PaisRegistry() {
        super();
        ConsoleLogger.info(lang.getString("pais-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<Pais> paises = dbManager.selectAll(Pais.class);
        if (paises != null) for (Pais p : paises) loadedObjects.put(p.getNombre(), p);
        ensureDefaults();
    }

    @Override
    public void load(Pais obj) {
        if (obj == null || obj.getNombre() == null) return;
        loadedObjects.put(obj.getNombre(), obj);
        dbManager.save(obj);
    }

    public Pais getArgentina() {
        return get("argentina");
    }

    public Pais getChile() {
        return get("chile");
    }

    public Pais getBolivia() {
        return get("bolivia");
    }

    public Pais getPeru() {
        return get("peru");
    }

    public Pais getParaguay() {
        return get("paraguay");
    }

    public Pais getUruguay() {
        return get("uruguay");
    }

    public List<Long> getDsGuildIds() {
        return loadedObjects.values().stream().map(Pais::getDsIdGuild).toList();
    }

    public List<Long> getDsLogIds() {
        return loadedObjects.values().stream().map(Pais::getDsIdLog).toList();
    }

    public List<Long> getDsGlobalChatIds() {
        return loadedObjects.values().stream().map(Pais::getDsIdGlobalChat).toList();
    }

    public List<Long> getDsCountryChatIds() {
        return loadedObjects.values().stream().map(Pais::getDsIdCountryChat ).toList();
    }
    public Pais findByDsGuildId(Long dsGuildId) {
        if (dsGuildId == null) return null;
        for (Pais pais : loadedObjects.values()) {
            if (dsGuildId.equals(pais.getDsIdGuild())) return pais;
        }
        return null;
    }

    public Pais findByDsGlobalChatId(Long dsGlobalChatId) {
        if (dsGlobalChatId == null) return null;
        for (Pais pais : loadedObjects.values()) {
            if (dsGlobalChatId.equals(pais.getDsIdGlobalChat())) return pais;
        }
        return null;
    }

    public Pais findByRequestId(Long dsRequestId) {
        if (dsRequestId == null) return null;
        for (Pais pais : loadedObjects.values()) {
            if (dsRequestId.equals(pais.getDsIdRequest())) return pais;
        }
        return null;
    }

    public Pais findByDsCountryChatId(Long dsCountryChatId) {
        if (dsCountryChatId == null) return null;
        for (Pais pais : loadedObjects.values()) {
            if (dsCountryChatId.equals(pais.getDsIdCountryChat())) return pais;
        }
        
        return null;
    }

    public Pais findByLocation(double x, double z) {
        for (Pais pais : loadedObjects.values()) {
            List<RegionPais> regiones = pais.getRegiones();
            if (regiones == null) continue;
            for (RegionPais region : regiones) {
                if (RegionUtils.containsCoordinate(region.getPoligono(), x, z)) {
                    return pais;
                }
            }
        }
        return null;
    }

    public Division findDivisionByLocation(double x, double z, Pais pais) {
        for (Division division : pais.getDivisiones()) {
            List<RegionDivision> regiones = division.getRegiones();
            if (regiones == null) continue;
            for (RegionDivision region : regiones) {
                if (RegionUtils.containsCoordinate(region.getPoligono(), x, z)) {
                    return division;
                }
            }
        }
        return null;
    }

    public Pais findByPolygon(Polygon polygon) {
        Point centroid = polygon.getCentroid();
        return findByLocation(centroid.getX(), centroid.getY());
    }   

    public Division findDivisionByPolygon(Polygon polygon, Pais pais) {
        Point centroid = polygon.getCentroid();
        Division division = findDivisionByLocation(centroid.getX(), centroid.getY(), pais);
        if (division == null) return getDefaultDivision(pais);
        return division;
    }

    public Division getDefaultDivision(Pais pais) {
        for (Division division : pais.getDivisiones()) {
            if (division.getNombre().equalsIgnoreCase("default")) return division;
        }
        ConsoleLogger.warn("No se encontró la ciudad Default para el país: " + pais.getNombre());
        return null;
    }
    //TODO: right click mensaje can build 

    private void ensureDefaults() {
        if (get("argentina") == null) {
            Pais pais = new Pais("argentina", "Argentina", 1425856269029474304L, 1451333771319050320L, 1451333825149014118L, 1451333852046950583L, 1451333884749807616L);
            load(pais);
        }
        if (get("chile") == null) {
            Pais pais = new Pais("chile", "Chile", 1425856269029474304L, 1451333916744093768L, 1451334085602447471L, 1451334251026055190L, 1451334378746544218L);
            load(pais);
        }
        if (get("bolivia") == null) {
            Pais pais = new Pais("bolivia", "Bolivia", 1425856269029474304L, 1451333943352885323L, 1451334123305046239L, 1451334269418082495L, 1451334392105406546L);
            load(pais);
        }
        if (get("peru") == null) {
            Pais pais = new Pais("peru", "Peru", 1425856269029474304L, 1451333984075255869L, 1451334156889096232L, 1451334297427378176L, 1451334413622448261L);
            load(pais);
        }
        if (get("paraguay") == null) {
            Pais pais = new Pais("paraguay", "Paraguay", 1425856269029474304L, 1451334011044626433L, 1451334186785968269L, 1451334324732432507L, 1451334430869422210L);
            load(pais);
        }
        if (get("uruguay") == null) {
            Pais pais = new Pais("uruguay", "Uruguay", 1425856269029474304L, 1451334055051399288L, 1451334227818713300L, 1451334350661484716L, 1451334465770229882L);
            load(pais);
        }

        ensureDefaultsDivisions();
    }

    private void ensureDefaultsDivisions() {
        for (Pais pais : loadedObjects.values()) {
            boolean hasDefault = false;
            for (Division division : pais.getDivisiones()) {
                if (division.getNombre().equalsIgnoreCase("default")) {
                    hasDefault = true;
                    break;
                }
            }
            if (!hasDefault) {
                Division defaultDivision = new Division(pais, "default", "Default",  "Division", "Default division", "N/A");
                pais.addDivision(defaultDivision);
                merge(pais.getNombre());
            }
        }
    }   

    public void shutdown() {
        ConsoleLogger.info(lang.getString("pais-registry-shutting-down"));
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
