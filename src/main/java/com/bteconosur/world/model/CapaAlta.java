package com.bteconosur.world.model;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldManager;

public class CapaAlta extends LabelWorld {

    private final List<RegionData> regions = new ArrayList<>();

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public CapaAlta(String name, String displayName, int offset) {
        super(name, displayName, offset);

        loadRegions();
        if (config.getBoolean("border-particles.label-enable")) enableParticlesSpawning();
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

    private Polygon getPolygonForPlayer(Player player) {
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

    private void enableParticlesSpawning() {
        long periodTicks = ConfigHandler.getInstance().getConfig().getLong("border-particles.spawn-period");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
                    if (!player.getConfiguration().getGeneralLabelBorder()) continue;
                    org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player.getUuid());
                    if (bukkitPlayer == null) continue;
                    if (WorldManager.getInstance().getBTEWorld().isLobbyLocation(bukkitPlayer.getLocation())) continue;
                    if (!player.getConfiguration().getGeneralPaisBorder()) continue;
                    
                    Polygon polygon = getPolygonForPlayer(player);
                    if (polygon == null) continue;

                    RegionUtils.spawnBorderParticles(bukkitPlayer, polygon, config.getString("border-particles.label-particle"));
                }
            }
        }.runTaskTimer(BTEConoSur.getInstance(), 0L, periodTicks);
    }
    
}
