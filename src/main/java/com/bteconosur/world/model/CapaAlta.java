package com.bteconosur.world.model;

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

    private final List<Polygon> regions;

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public CapaAlta(String name, String displayName, int offset) {
        super(name, displayName, offset);

        this.regions = loadRegions();
        if (config.getBoolean("border-particles.label-enable")) enableParticlesSpawning();
    }

    private List<Polygon> loadRegions() {
        return GeoJsonUtils.geoJsonToPolygons("world", getName() + ".geojson");
    }

    public List<Polygon> getRegions() {
        return this.regions;
    }

    private Polygon getPolygonForPlayer(Player player) {
        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player.getUuid());
        if (bukkitPlayer == null) return null;

        double x = bukkitPlayer.getLocation().getX();
        double z = bukkitPlayer.getLocation().getZ();

        for (Polygon polygon : regions) {
            if (RegionUtils.containsCoordinate(polygon, x, z)) {
                return polygon;
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
