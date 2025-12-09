package com.bteconosur.world.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;

public class LabelWorld {

    private final String name;
    private final String displayName;
    private final int offset;

    private final World bukkitWorld;
    private final Geometry region;

    private ConsoleLogger logger = BTEConoSur.getConsoleLogger();
    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();;

    public LabelWorld(String name, String displayName, int offset) {
        String msg = lang.getString("label-world-loading").replace("%name%", name).replace("%offset%", String.valueOf(offset));
        logger.info(msg);

        this.name = name;
        this.displayName = displayName;
        this.offset = offset;

        bukkitWorld = BTEConoSur.getInstance().getServer().getWorld(name);
        if (bukkitWorld == null) logger.error("La capa '" + name + "' no está creada en el servidor.");

        // Null si ocurre un error al cargar la región
        this.region = loadRegionFromConfig();
    }

    private Geometry loadRegionFromConfig() {
        YamlConfiguration worlds = ConfigHandler.getInstance().getWorlds();
        if (!worlds.contains(name)) {
            logger.error("No se ha encontrado en worlds.yml la región del mundo: " + name);
            return null;
        }

        ConfigurationSection sec = worlds.getConfigurationSection(name);

        GeometryFactory gf = new GeometryFactory();
        Geometry combined = null;

        for (String key : sec.getKeys(false)) {
            List<?> raw = sec.getList(key);
            if (raw == null || raw.size() < 3) continue;

            List<Coordinate> coords = new ArrayList<>();
            for (Object o : raw) {
                if (!(o instanceof Map)) {
                    logger.error("Punto inválido en worlds.yml (" + name + ", id: " + key + "): " + String.valueOf(o));
                    return null;
                }
                Map<?,?> m = (Map<?,?>) o;
                Object ox = m.get("x");
                Object oz = m.get("z");
                if (!(ox instanceof Number) || !(oz instanceof Number)) {
                    logger.error("Punto inválido en worlds.yml (" + name + ", id: " + key + "): x/z no son numéricos - x=" + ox + " z=" + oz);
                    return null;
                }
                double x = ((Number) ox).doubleValue();
                double z = ((Number) oz).doubleValue();
                coords.add(new Coordinate(x, z));
            }

            if (coords.size() < 3) continue;

            Coordinate first = coords.get(0);
            Coordinate last = coords.get(coords.size() - 1);
            if (!first.equals2D(last)) {
                coords.add(new Coordinate(first.x, first.y));
            }

            try {
                Coordinate[] arr = coords.toArray(new Coordinate[0]);
                LinearRing shell = gf.createLinearRing(arr);
                Polygon poly = gf.createPolygon(shell, null);
                if (combined == null) combined = poly;
                else combined = combined.union(poly);
            } catch (Exception e) {
                logger.error("Polígono inválido en worlds.yml (" + name + ", id: " + key + "): " + e);
                return null;
            }
        }

        return combined;
    }

    public void teleportPlayer(Player player, double x, double y, double z, float yaw, float pitch) {
        if (bukkitWorld == null) return;
        player.teleport(new Location(bukkitWorld, x, y, z, yaw, pitch));
    }

    public Geometry getRegion() {
        return this.region;
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
}
