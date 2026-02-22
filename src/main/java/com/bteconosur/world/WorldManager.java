package com.bteconosur.world;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PluginRegistry;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.world.model.BTEWorld;
import com.bteconosur.world.model.LabelWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class WorldManager {

    private static WorldManager instance;

    public static StateFlag REVIEWER_COUNTRY;

    private BTEWorld bteWorld;
    private RegionContainer worldGuardContainer;
    private Set<Material> bannedItems;

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public WorldManager() {

        ConsoleLogger.info(LanguageHandler.getText("world-module-initializing"));

        try {
            worldGuardContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        } catch (Exception e) {
            e.printStackTrace();
            PluginRegistry.disablePlugin("WORLDGUARD_LOAD_ERROR");
        }
        if (worldGuardContainer == null)
            PluginRegistry.disablePlugin("WORLDGUARD_LOAD_ERROR");
        
        bannedItems = config.getStringList("banned-items").stream()
            .map(String::toUpperCase)
            .filter(name -> {
                try {
                    return Material.valueOf(name).isItem();
                } catch (IllegalArgumentException e) {
                    ConsoleLogger.warn("Material no válido en la lista de items prohibidos: " + name);
                    return false;
                }
            })
            .map(Material::valueOf)
            .collect(java.util.stream.Collectors.toSet());
    
        bteWorld = new BTEWorld();
    }

    private RegionManager getRegionManager(Proyecto proyecto) {
        Polygon polygon = proyecto.getPoligono();
        Point centroid = polygon.getCentroid();
        LabelWorld labelWorld = bteWorld.getLabelWorld(centroid.getX(), centroid.getY());
        if (labelWorld == null) {
            ConsoleLogger.warn("No se pudo obtener el LabelWorld para el proyecto " + proyecto.getId());
            return null;
        }
        return labelWorld.getRegionManager();
    }

    private ProtectedRegion getRegion(Proyecto proyecto) {
        RegionManager regionManager = getRegionManager(proyecto);
        if (regionManager == null) return null;
        ProtectedRegion region = regionManager.getRegion(config.getString("wg-proyecto-prefix") + proyecto.getId());
        if (region == null) {
            ConsoleLogger.warn("Region no encontrada: " + config.getString("wg-proyecto-prefix") + proyecto.getId());
            return null;
        }
        return region;
    }

    public void createRegion(Proyecto proyecto) {
        RegionManager regionContainer = getRegionManager(proyecto);
        ProtectedPolygonalRegion region = RegionUtils.toProtectedRegion(proyecto.getPoligono(), config.getString("wg-proyecto-prefix") + proyecto.getId());
        DefaultDomain members = region.getMembers();
        Player lider = ProjectManager.getInstance().getLider(proyecto);
        if (lider != null) {
            members.addPlayer(lider.getUuid());
            region.setMembers(members);
        }
        region.setPriority(1);
        ProtectedRegion parentProject = regionContainer.getRegion(config.getString("wg-parent-proyecto"));
        try {
            region.setParent(parentProject);
        } catch (CircularInheritanceException e) {
            ConsoleLogger.error("Error al establecer la región padre para el proyecto " + proyecto.getId());
            e.printStackTrace();
        }
        regionContainer.addRegion(region);
    }

    public void createRegion(Proyecto proyecto, DefaultDomain members) {
        RegionManager regionContainer = getRegionManager(proyecto);
        ProtectedPolygonalRegion region = RegionUtils.toProtectedRegion(proyecto.getPoligono(), config.getString("wg-proyecto-prefix") + proyecto.getId());
        region.setMembers(members);
        ProtectedRegion parentProject = regionContainer.getRegion(config.getString("wg-parent-proyecto"));
        try {
            region.setParent(parentProject);
        } catch (CircularInheritanceException e) {
            ConsoleLogger.error("Error al establecer la región padre para el proyecto " + proyecto.getId());
            e.printStackTrace();
        }
        regionContainer.addRegion(region);
    }

    public void removeRegion(Proyecto proyecto) {
        RegionManager regionContainer = getRegionManager(proyecto);
        regionContainer.removeRegion(config.getString("wg-proyecto-prefix") + proyecto.getId());
    }

    public void addPlayer(Proyecto proyecto, UUID playerUuid) {
        RegionManager regionContainer = getRegionManager(proyecto);
        ProtectedRegion region = getRegion(proyecto);
        if (region == null) return;
        DefaultDomain members = region.getMembers();
        members.addPlayer(playerUuid);
        region.setMembers(members);
        regionContainer.addRegion(region);
    }

    public void addPlayers(Proyecto proyecto) {
        RegionManager regionContainer = getRegionManager(proyecto);
        ProtectedRegion region = getRegion(proyecto);
        if (region == null) return;
        DefaultDomain members = region.getMembers();
        ProjectManager pm = ProjectManager.getInstance();
        Set<Player> miembros = pm.getMembers(proyecto);
        Player lider = pm.getLider(proyecto);
        if (lider != null) members.addPlayer(lider.getUuid());
        for (Player miembro : miembros) {
            members.addPlayer(miembro.getUuid());
        }
        region.setMembers(members);
        regionContainer.addRegion(region);
    }

    public void removePlayers(Proyecto proyecto) {
        RegionManager regionContainer = getRegionManager(proyecto);
        ProtectedRegion region = getRegion(proyecto);
        if (region == null) return;
        DefaultDomain members = region.getMembers();
        ProjectManager pm = ProjectManager.getInstance();
        Set<Player> miembros = pm.getMembers(proyecto);
        Player lider = pm.getLider(proyecto);
        if (lider != null) members.removePlayer(lider.getUuid());
        for (Player miembro : miembros) {
            members.removePlayer(miembro.getUuid());
        }
        region.setMembers(members);
        regionContainer.addRegion(region);
    }

    public void removePlayer(Proyecto proyecto, UUID playerUuid) {
        if (proyecto == null) return;
        RegionManager regionContainer = getRegionManager(proyecto);
        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) getRegion(proyecto);
        if (region == null) return;
        DefaultDomain members = region.getMembers();
        members.removePlayer(playerUuid);
        region.setMembers(members);
        regionContainer.addRegion(region);
    }

    public void updateRegion(Proyecto proyecto) {
        DefaultDomain members = getRegion(proyecto).getMembers();
        removeRegion(proyecto);
        createRegion(proyecto, members);
    }

    public boolean hasPlayerInRegion(Proyecto proyecto, UUID playerUuid) {
        ProtectedRegion region = getRegion(proyecto);
        if (region == null) return false;
        return region.getMembers().contains(playerUuid);
    }

    public void syncRegions() {
        for (Proyecto proyecto : ProyectoRegistry.getInstance().getList()) {
            ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) getRegion(proyecto);
            if (region == null) {
                ConsoleLogger.info("Sincronizando región no creada del proyecto " + proyecto.getId());
                createRegion(proyecto);
            }
            RegionManager regionContainer = getRegionManager(proyecto);
            region = (ProtectedPolygonalRegion) getRegion(proyecto);
            ProtectedRegion parentProject = regionContainer.getRegion(config.getString("wg-parent-proyecto"));
            try {
                region.setParent(parentProject);
            } catch (CircularInheritanceException e) {
                ConsoleLogger.error("Error al establecer la región padre para el proyecto " + proyecto.getId());
                e.printStackTrace();
            }
            region.setPriority(1);
            DefaultDomain members = region.getMembers();
            if (!RegionUtils.sameShape(proyecto.getPoligono(), region)) {
                ConsoleLogger.info("La región del proyecto " + proyecto.getId() + " no tiene la misma forma que el polígono del proyecto");
                removeRegion(proyecto);
                createRegion(proyecto, members);
                region = (ProtectedPolygonalRegion) getRegion(proyecto);
            }
            if (proyecto.getEstado() != Estado.ACTIVO && proyecto.getEstado() != Estado.EDITANDO) {
                Set<UUID> reviewersConToggle = ReviewerToggleBuildService.getReviewersForProject(proyecto.getId());
                ProjectManager pm = ProjectManager.getInstance();
                Set<Player> miembros = pm.getMembers(proyecto);
                Player lider = pm.getLider(proyecto);
                
                if (lider != null) members.removePlayer(lider.getUuid());
                for (Player miembro : miembros) {
                    members.removePlayer(miembro.getUuid());
                }
                
                for (UUID reviewerUuid : reviewersConToggle) {
                    Player reviewer = PlayerRegistry.getInstance().get(reviewerUuid);
                    if (reviewer != null) {
                        ConsoleLogger.info("Manteniendo reviewer " + reviewer.getNombre() + " con toggle activo en proyecto no activo " + proyecto.getId());
                        members.addPlayer(reviewerUuid);
                    }
                }
                
                region.setMembers(members);
                regionContainer.addRegion(region);
                continue;
            }
            ProjectManager pm = ProjectManager.getInstance();
            Set<Player> miembros = pm.getMembers(proyecto);
            Player lider = pm.getLider(proyecto);
            if (lider != null) miembros.add(lider);
            Set<UUID> miembrosUuid = members.getUniqueIds();
            
            for (Player miembro : miembros) {
                if (!miembrosUuid.contains(miembro.getUuid())) {
                    ConsoleLogger.info("Agregando jugador " + miembro.getNombre() + " a la región del proyecto " + proyecto.getId());
                    members.addPlayer(miembro.getUuid());
                }
            }
            
            Set<UUID> reviewersConToggle = ReviewerToggleBuildService.getReviewersForProject(proyecto.getId());
            for (UUID reviewerUuid : reviewersConToggle) {
                if (!miembrosUuid.contains(reviewerUuid)) {
                    Player reviewer = PlayerRegistry.getInstance().get(reviewerUuid);
                    if (reviewer != null) {
                        ConsoleLogger.info("Agregando reviewer " + reviewer.getNombre() + " con toggle activo a la región del proyecto " + proyecto.getId());
                        members.addPlayer(reviewerUuid);
                    }
                }
            }
            
            miembrosUuid = members.getUniqueIds();
            for (UUID miembroUuid : miembrosUuid) {
                boolean encontrado = false;
                for (Player miembro : miembros) {
                    if (miembro.getUuid().equals(miembroUuid)) {
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) {
                    String toggledProyectoId = ReviewerToggleBuildService.getBuildEnabled(miembroUuid);
                    if (toggledProyectoId != null && toggledProyectoId.equals(proyecto.getId())) {
                        Player reviewer = PlayerRegistry.getInstance().get(miembroUuid);
                        if (reviewer != null) {
                            ConsoleLogger.info("Manteniendo reviewer " + reviewer.getNombre() + " con toggle activo en la región del proyecto " + proyecto.getId());
                        }
                    } else {
                        ConsoleLogger.info("Removiendo jugador " + miembroUuid.toString() + " de la región del proyecto " + proyecto.getId());
                        members.removePlayer(miembroUuid);
                    }
                }
            }
            region.setMembers(members);
            regionContainer.addRegion(region);
        }
    }

    public void checkLayerMove(Location lFrom, Location lTo, org.bukkit.entity.Player player) {
        if (bteWorld == null || !bteWorld.isValid()) return;
        if (lTo.getWorld().getName().equalsIgnoreCase("lobby")) return;
        bteWorld.checkLayerMove(lFrom, lTo, player);
    }

    public boolean checkPaisMove(Location lFrom, Location lTo, org.bukkit.entity.Player player) {
        if (lTo.getWorld().getName().equalsIgnoreCase("lobby")) return true;
        return bteWorld.checkPaisMove(lFrom, lTo, player);
    }

    public BTEWorld getBTEWorld() {
        return this.bteWorld;
    }

    public RegionContainer getWorldGuardContainer() {
        return this.worldGuardContainer;
    }

    public Set<Material> getBannedItems() {
        return this.bannedItems;
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("world-module-shutting-down"));
        bteWorld.shutdown();
    }

    public static WorldManager getInstance() {
        if (instance == null) {
            instance = new WorldManager();
        }
        return instance;
    }

}
