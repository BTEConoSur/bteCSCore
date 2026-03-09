package com.bteconosur.world.model;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.RegionUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

/**
 * Clase abstracta que representa una capa del mundo BTE.
 * Gestiona la integración con WorldGuard y las regiones geográficas.
 */
public abstract class LabelWorld {

    private final String name;
    private final String displayName;
    private final int offset;

    private final World bukkitWorld;
    private final RegionManager regionManager;

    private final List<RegionData> regions = new ArrayList<>();

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    /**
     * Constructor de un LabelWorld.
     * Inicializa el mundo, el gestor de regiones de WorldGuard y carga las regiones geográficas.
     * 
     * @param name Nombre del mundo de Bukkit
     * @param displayName Nombre para mostrar al jugador
     * @param offset Offset vertical de la capa
     */
    public LabelWorld(String name, String displayName, int offset) {
        String msg = LanguageHandler.getText("label-world-loading").replace("%name%", name).replace("%offset%", String.valueOf(offset));
        ConsoleLogger.info(msg);

        this.name = name;
        this.displayName = displayName;
        this.offset = offset;
        bukkitWorld = BTEConoSur.getInstance().getServer().getWorld(name);
        if (bukkitWorld == null) ConsoleLogger.error(LanguageHandler.getText("invalid-label-world").replace("%name%", name));
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        regionManager = regionContainer.get(BukkitAdapter.adapt(bukkitWorld));
        ProtectedRegion parentProject = regionManager.getRegion(config.getString("wg-parent-proyecto"));
        if (parentProject == null) {
            ConsoleLogger.warn("La región padre de proyectos " + config.getString("wg-parent-proyecto") + " no existe en WorldGuard. Creándola...");
            parentProject = new GlobalProtectedRegion(config.getString("wg-parent-proyecto"));
            regionManager.addRegion(parentProject);
        }
        loadRegions();
    }

    /**
     * Teletransporta un jugador a una ubicación específica en este mundo.
     * 
     * @param player Jugador a teletransportar
     * @param x Coordenada X
     * @param y Coordenada Y
     * @param z Coordenada Z
     * @param yaw Rotación horizontal
     * @param pitch Rotación vertical
     */
    public void teleportPlayer(Player player, double x, double y, double z, float yaw, float pitch) {
        if (bukkitWorld == null) return;
        player.teleport(new Location(bukkitWorld, x, y, z, yaw, pitch));
    }

    /**
     * Verifica si una ubicación pertenece a este mundo.
     * 
     * @param location Ubicación a verificar
     * @return true si la ubicación es válida para este mundo, false en caso contrario
     */
    public boolean isValidLocation(Location location) {
        if (location == null) return false;
        if (bukkitWorld == null) return false;
        return location.getWorld().equals(bukkitWorld);
    }

    /**
     * Carga las regiones geográficas desde archivos GeoJSON.
     */
    private void loadRegions() {
        List<Polygon> polygons = GeoJsonUtils.geoJsonToPolygons("world", getName() + ".geojson");
        for (Polygon polygon : polygons) {
            regions.add(new RegionData(polygon));
        }
        return;
    }

    /**
     * Obtiene la lista de regiones geográficas de este mundo.
     * 
     * @return Lista de RegionData
     */
    public List<RegionData> getRegions() {
        return this.regions;
    }

    /**
     * Obtiene el polígono de la región en la que se encuentra un jugador.
     * 
     * @param player Jugador del cual obtener el polígono
     * @return Polígono que contiene la ubicación del jugador, o null si no está en ninguna región
     */
    public Polygon getPolygonForPlayer(com.bteconosur.db.model.Player player) {
        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player.getUuid());
        if (bukkitPlayer == null) return null;

        double x = bukkitPlayer.getLocation().getX();
        double z = bukkitPlayer.getLocation().getZ();

        for (RegionData regionData : regions) {
            if (RegionUtils.containsCoordinate(regionData.getPrepared(), regionData.getEnvelope(), x, z)) {
                return regionData.getPolygon();
            }
        }
        return null;
    }

    /**
     * Obtiene el mundo de Bukkit asociado a esta capa.
     * 
     * @return Instancia del mundo de Bukkit
     */
    public World getBukkitWorld() {
        return this.bukkitWorld;
    }

    /**
     * Obtiene el nombre del mundo.
     * 
     * @return Nombre del mundo
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtiene el nombre para mostrar del mundo.
     * 
     * @return Nombre de visualización
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Obtiene el offset vertical de la capa.
     * 
     * @return Offset vertical
     */
    public int getOffset() {
        return this.offset;
    }
    
    /**
     * Obtiene el gestor de regiones de WorldGuard para este mundo.
     * 
     * @return RegionManager de WorldGuard
     */
    public RegionManager getRegionManager() {
        return this.regionManager;
    }
}
