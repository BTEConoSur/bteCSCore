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

/**
 * Gestor principal del sistema de mundos, regiones y permisos de construcción.
 * Administra la integración con WorldGuard para el control de regiones de proyectos.
 * Implementa el patrón Singleton.
 */
public class WorldManager {

    private static WorldManager instance;

    public static StateFlag REVIEWER_COUNTRY;

    private BTEWorld bteWorld;
    private RegionContainer worldGuardContainer;
    private Set<Material> bannedItems;

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    /**
     * Constructor del WorldManager.
     * Inicializa el contenedor de WorldGuard, carga los items prohibidos y crea el BTEWorld.
     */
    public WorldManager() {

        ConsoleLogger.info(LanguageHandler.getText("world-module-initializing"));

        try {
            worldGuardContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        } catch (Exception e) {
            ConsoleLogger.error("Error cargando WorldGuard: ", e);
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

    /**
     * Obtiene el gestor de regiones de WorldGuard correspondiente al mundo del proyecto.
     * 
     * @param proyecto Proyecto del cual obtener el gestor de regiones
     * @return RegionManager del mundo correspondiente, o null si el proyecto no tiene polígono o no se encuentra el mundo
     */
    private RegionManager getRegionManager(Proyecto proyecto) {
        Polygon polygon = proyecto.getPoligono();
        if (polygon == null) {
            ConsoleLogger.info("El proyecto " + proyecto.getId() + " no tiene polígono asignado.");
            return null;
        }
        Point centroid = polygon.getCentroid();
        LabelWorld labelWorld = bteWorld.getLabelWorld(centroid.getX(), centroid.getY());
        if (labelWorld == null) {
            ConsoleLogger.info("No se pudo obtener el LabelWorld para el proyecto " + proyecto.getId());
            return null;
        }
        return labelWorld.getRegionManager();
    }

    /**
     * Obtiene la región protegida de WorldGuard asociada a un proyecto.
     * 
     * @param proyecto Proyecto del cual obtener la región
     * @return ProtectedRegion del proyecto, o null si no existe
     */
    private ProtectedRegion getRegion(Proyecto proyecto) {
        RegionManager regionManager = getRegionManager(proyecto);
        if (regionManager == null) return null;
        ProtectedRegion region = regionManager.getRegion(config.getString("wg-proyecto-prefix") + proyecto.getId());
        if (region == null) {
            ConsoleLogger.info("Region no encontrada: " + config.getString("wg-proyecto-prefix") + proyecto.getId());
            return null;
        }
        return region;
    }

    /**
     * Crea una región de WorldGuard para un proyecto, añadiendo automáticamente al líder como miembro.
     * 
     * @param proyecto Proyecto para el cual crear la región
     */
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
            ConsoleLogger.error("Error al establecer la región padre para el proyecto " + proyecto.getId(), e);
        }
        regionContainer.addRegion(region);
    }

    /**
     * Crea una región de WorldGuard para un proyecto con un dominio de miembros específico.
     * 
     * @param proyecto Proyecto para el cual crear la región
     * @param members Dominio de miembros que tendrán permisos en la región
     */
    public void createRegion(Proyecto proyecto, DefaultDomain members) {
        RegionManager regionContainer = getRegionManager(proyecto);
        ProtectedPolygonalRegion region = RegionUtils.toProtectedRegion(proyecto.getPoligono(), config.getString("wg-proyecto-prefix") + proyecto.getId());
        region.setMembers(members);
        ProtectedRegion parentProject = regionContainer.getRegion(config.getString("wg-parent-proyecto"));
        try {
            region.setParent(parentProject);
        } catch (CircularInheritanceException e) {
            ConsoleLogger.error("Error al establecer la región padre para el proyecto " + proyecto.getId(), e);
        }
        regionContainer.addRegion(region);
    }

    /**
     * Elimina la región de WorldGuard asociada a un proyecto.
     * 
     * @param proyecto Proyecto cuya región se eliminará
     */
    public void removeRegion(Proyecto proyecto) {
        RegionManager regionContainer = getRegionManager(proyecto);
        regionContainer.removeRegion(config.getString("wg-proyecto-prefix") + proyecto.getId());
    }

    /**
     * Añade un jugador a los miembros de la región de un proyecto.
     * 
     * @param proyecto Proyecto al cual añadir el jugador
     * @param playerUuid UUID del jugador a añadir
     */
    public void addPlayer(Proyecto proyecto, UUID playerUuid) {
        RegionManager regionContainer = getRegionManager(proyecto);
        ProtectedRegion region = getRegion(proyecto);
        if (region == null) return;
        DefaultDomain members = region.getMembers();
        members.addPlayer(playerUuid);
        region.setMembers(members);
        regionContainer.addRegion(region);
    }

    /**
     * Añade todos los miembros y el líder de un proyecto a la región de WorldGuard.
     * 
     * @param proyecto Proyecto cuyos miembros se añadirán
     */
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

    /**
     * Remueve todos los miembros y el líder de un proyecto de la región de WorldGuard.
     * 
     * @param proyecto Proyecto cuyos miembros se removerán
     */
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

    /**
     * Remueve un jugador específico de los miembros de la región de un proyecto.
     * 
     * @param proyecto Proyecto del cual remover el jugador
     * @param playerUuid UUID del jugador a remover
     */
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

    /**
     * Actualiza la región de un proyecto, preservando los miembros actuales.
     * Se utiliza cuando cambia la forma o ubicación del proyecto.
     * 
     * @param proyecto Proyecto cuya región se actualizará
     */
    public void updateRegion(Proyecto proyecto) {
        DefaultDomain members = getRegion(proyecto).getMembers();
        removeRegion(proyecto);
        createRegion(proyecto, members);
    }

    /**
     * Verifica si un jugador es miembro de la región de un proyecto.
     * 
     * @param proyecto Proyecto a verificar
     * @param playerUuid UUID del jugador
     * @return true si el jugador es miembro de la región, false en caso contrario
     */
    public boolean hasPlayerInRegion(Proyecto proyecto, UUID playerUuid) {
        ProtectedRegion region = getRegion(proyecto);
        if (region == null) return false;
        return region.getMembers().contains(playerUuid);
    }

    /**
     * Sincroniza todas las regiones de proyectos con la base de datos.
     * Crea regiones faltantes, actualiza formas desactualizadas y ajusta miembros según el estado del proyecto.
     * Los revisores con toggle activo se mantienen incluso en proyectos no activos.
     */
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
                ConsoleLogger.error("Error al establecer la región padre para el proyecto " + proyecto.getId(), e);
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

    /**
     * Verifica el movimiento de un jugador para gestionar el cambio de capas.
     * 
     * @param lTo Ubicación de destino
     * @param player Jugador que se mueve
     */
    public void checkMove(Location lTo, org.bukkit.entity.Player player) {
        if (bteWorld == null || !bteWorld.isValid()) return;
        if (lTo.getWorld().getName().equalsIgnoreCase(config.getString("lobby.world"))) return;
        bteWorld.checkLayerMove(lTo, player);
    }

    /**
     * Verifica el movimiento de un jugador para mostrar títulos de proyecto y división.
     * 
     * @param lTo Ubicación de destino
     * @param player Jugador que se mueve
     */
    public void checkTitles(Location lTo, org.bukkit.entity.Player player) {
        if (bteWorld == null || !bteWorld.isValid()) return;
        if (lTo.getWorld().getName().equalsIgnoreCase(config.getString("lobby.world"))) return;
        bteWorld.checkProyectoMove(lTo, player);
        bteWorld.checkDivisionMove(lTo, player);
    }

    /**
     * Verifica si un jugador puede moverse de una ubicación a otra sin salir de los límites de los países.
     * 
     * @param lFrom Ubicación de origen
     * @param lTo Ubicación de destino
     * @param player Jugador que se mueve
     * @return true si el movimiento es válido, false si está intentando salir de los límites
     */
    public boolean checkPaisMove(Location lFrom, Location lTo, org.bukkit.entity.Player player) {
        if (config.getBoolean("tp-outside-country")) return true;
        if (lTo.getWorld().getName().equalsIgnoreCase(config.getString("lobby.world"))) return true;
        return bteWorld.checkPaisMove(lFrom, lTo, player);
    }

    /**
     * Obtiene la instancia del mundo BTE.
     * 
     * @return Instancia de BTEWorld
     */
    public BTEWorld getBTEWorld() {
        return this.bteWorld;
    }

    /**
     * Obtiene el contenedor de regiones de WorldGuard.
     * 
     * @return RegionContainer de WorldGuard
     */
    public RegionContainer getWorldGuardContainer() {
        return this.worldGuardContainer;
    }

    /**
     * Obtiene el conjunto de items prohibidos en el servidor.
     * 
     * @return Conjunto de materiales prohibidos
     */
    public Set<Material> getBannedItems() {
        return this.bannedItems;
    }

    /**
     * Apaga el gestor de mundos, cancelando todas las tareas programadas.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("world-module-shutting-down"));
        bteWorld.shutdown();
    }

    /**
     * Obtiene la instancia singleton del WorldManager.
     * 
     * @return Instancia única de WorldManager
     */
    public static WorldManager getInstance() {
        if (instance == null) {
            instance = new WorldManager();
        }
        return instance;
    }

}
