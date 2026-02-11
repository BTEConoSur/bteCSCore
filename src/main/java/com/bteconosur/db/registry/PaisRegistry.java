package com.bteconosur.db.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RegionDivision;
import com.bteconosur.db.model.RegionPais;
import com.bteconosur.world.WorldManager;

public class PaisRegistry extends Registry<Long, Pais> {

    private static PaisRegistry instance;

    private ConcurrentHashMap<Long, List<RegionPais>> loadedRegions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, List<Division>> loadedDivisions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, List<RegionDivision>> loadedRegionDivisions = new ConcurrentHashMap<>();

    public PaisRegistry() {
        super();
        ConsoleLogger.info(lang.getString("pais-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<Pais> paises = dbManager.selectAll(Pais.class);
        if (paises != null) for (Pais p : paises) loadedObjects.put(p.getId(), p);

        List<RegionPais> todasRegiones = dbManager.selectAll(RegionPais.class);
        List<Division> todasDivisiones = dbManager.selectAll(Division.class);
        List<RegionDivision> todasRegionDivisiones = dbManager.selectAll(RegionDivision.class);

        if (todasRegiones != null) {
            for (RegionPais region : todasRegiones) {
                Long paisId = region.getPais().getId();
                loadedRegions.computeIfAbsent(paisId, k -> new ArrayList<>()).add(region);
            }
        }

        if (todasDivisiones != null) {
            for (Division division : todasDivisiones) {
                Long paisId = division.getPais().getId();
                loadedDivisions.computeIfAbsent(paisId, k -> new ArrayList<>()).add(division);
            }
        }

        if (todasRegionDivisiones != null) {
            for (RegionDivision region : todasRegionDivisiones) {
                Long divisionId = region.getDivision().getId();
                loadedRegionDivisions.computeIfAbsent(divisionId, k -> new ArrayList<>()).add(region);
            }
        }

        ensureDefaults();
        //enableParticlesSpawning();
    }

    @Override
    public void load(Pais obj) {
        if (obj == null || obj.getId() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
    }

    public Pais get(String name) {
        for (Pais pais : loadedObjects.values()) {
            if (pais.getNombre().equalsIgnoreCase(name)) {
                return pais;
            }
        }
        return null;
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

    public List<Division> getDivisions(Pais pais) {
        return loadedDivisions.get(pais.getId());
    }

    public List<RegionPais> getRegions(Pais pais) {
        return loadedRegions.get(pais.getId());
    }

    public List<RegionDivision> getRegionDivisions(Division division) {
        return loadedRegionDivisions.get(division.getId());
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
            List<RegionPais> regiones = getRegions(pais);
            if (regiones == null) continue;
            for (RegionPais region : regiones) {
                if (RegionUtils.containsCoordinate(region.getPoligono(), x, z)) {
                    return pais;
                }
            }
        }
        return null;
    }

    public RegionPais findRegionByLocation(double x, double z) {
        for (Pais pais : loadedObjects.values()) {
            List<RegionPais> regiones = getRegions(pais);
            if (regiones == null) continue;
            for (RegionPais region : regiones) {
                if (RegionUtils.containsCoordinate(region.getPoligono(), x, z)) {
                    return region;
                }
            }
        }
        return null;
    }

    public Division findDivisionByLocation(double x, double z, Pais pais) {
        for (Division division : getDivisions(pais)) {
            List<RegionDivision> regiones = getRegionDivisions(division);
            if (regiones == null) continue;
            for (RegionDivision region : regiones) {
                if (RegionUtils.containsCoordinate(region.getPoligono(), x, z)) {
                    return division;
                }
            }
        }
        return null;
    }

    public Division findDivisionById(Long divisionId) {
        for (List<Division> divisions : loadedDivisions.values()) {
            for (Division division : divisions) {
                if (division.getId().equals(divisionId)) return division;
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
        for (Division division : getDivisions(pais)) { // Capaz no hace falta mapaear esto.
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
            for (Division division : getDivisions(pais)) {
                if (division.getNombre().equalsIgnoreCase("default")) {
                    hasDefault = true;
                    break;
                }
            }
            if (!hasDefault) {
                Division defaultDivision = new Division(pais, "default", "Default",  "Division", "Default division", "N/A");
                loadedDivisions.get(pais.getId()).add(defaultDivision);
                dbManager.save(defaultDivision);
            }
        }
    }

    private void enableParticlesSpawning() {
        long periodTicks = ConfigHandler.getInstance().getConfig().getLong("border-particles.spawn-period");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
                    if (!player.getConfiguration().getGeneralPaisBorder()) continue;
                    org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player.getUuid());
                    if (bukkitPlayer == null) continue;
                    if (WorldManager.getInstance().getBTEWorld().isLobbyLocation(bukkitPlayer.getLocation())) continue;
                    RegionPais region = findRegionByLocation(bukkitPlayer.getLocation().getX(), bukkitPlayer.getLocation().getZ());
                    if (region == null) continue;
                    Polygon polygon = region.getPoligono();
                    String particleName = config.getString("border-particles.pais-particle");
                    RegionUtils.spawnBorderParticles(bukkitPlayer, polygon, particleName);
                }
            }
        }.runTaskTimer(BTEConoSur.getInstance(), 0L, periodTicks);
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
