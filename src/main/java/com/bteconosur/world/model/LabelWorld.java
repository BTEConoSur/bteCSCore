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

public abstract class LabelWorld {

    private final String name;
    private final String displayName;
    private final int offset;

    private final World bukkitWorld;
    private final RegionManager regionManager;

    private final List<RegionData> regions = new ArrayList<>();

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

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

    public void teleportPlayer(Player player, double x, double y, double z, float yaw, float pitch) {
        if (bukkitWorld == null) return;
        player.teleport(new Location(bukkitWorld, x, y, z, yaw, pitch));
    }

    public boolean isValidLocation(Location location) {
        if (location == null) return false;
        if (bukkitWorld == null) return false;
        return location.getWorld().equals(bukkitWorld);
    }

    private void loadRegions() {
        List<Polygon> polygons = GeoJsonUtils.geoJsonToPolygons("world", getName() + ".geojson");
        for (Polygon polygon : polygons) {
            regions.add(new RegionData(polygon));
        }
        return;
    }

    public List<RegionData> getRegions() {
        return this.regions;
    }

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

    public World getBukkitWorld() {
        return this.bukkitWorld;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getOffset() {
        return this.offset;
    }
    
    public RegionManager getRegionManager() {
        return this.regionManager;
    }
}
