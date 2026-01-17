package com.bteconosur.core.util;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;

public class RegionUtils {

    private static final GeometryFactory gf = new GeometryFactory();
    private static final ConsoleLogger logger = BTEConoSur.getConsoleLogger();
    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public static Polygon toPolygon(CuboidRegion region) {
        return gf.createPolygon(new Coordinate[] {
            new Coordinate(region.getMinimumPoint().getX(), region.getMinimumPoint().getZ()),
            new Coordinate(region.getMinimumPoint().getX(), region.getMaximumPoint().getZ()),
            new Coordinate(region.getMaximumPoint().getX(), region.getMaximumPoint().getZ()),
            new Coordinate(region.getMaximumPoint().getX(), region.getMinimumPoint().getZ()),
            new Coordinate(region.getMinimumPoint().getX(), region.getMinimumPoint().getZ())
        });
    }

    public static Polygon toPolygon(Polygonal2DRegion region) {
        int size = region.getPoints().size();
        Coordinate[] coords = new Coordinate[size + 1];
        for (int i = 0; i < size; i++) {
            coords[i] = new Coordinate(region.getPoints().get(i).getX(), region.getPoints().get(i).getZ());
        }
        coords[size] = coords[0];
        return gf.createPolygon(coords);
    }

    public static Polygon getPolygon(CommandSender sender) {
        Player player = (Player) sender;
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
        Polygon regionPolygon = null;
        try {
            Region region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));

            if (region instanceof CuboidRegion) {
                CuboidRegion cuboid = (CuboidRegion) region;
                regionPolygon = toPolygon(cuboid);

            } else if (region instanceof Polygonal2DRegion) {
                Polygonal2DRegion poly = (Polygonal2DRegion) region;
                if (poly.getPoints().size() < 3) {
                    PlayerLogger.error(player, lang.getString("region-not-complete"), (String) null);
                    return null;
                }
                regionPolygon = toPolygon(poly);
            } else {
                PlayerLogger.error(player, lang.getString("region-not-supported"), (String) null);
                return null;
            }
        } catch (IncompleteRegionException e) {
            PlayerLogger.error(player, lang.getString("region-not-selected"), (String) null);
            return null;
        } catch (Exception e) {
            PlayerLogger.error(player, lang.getString("we-error"), (String) null);
            logger.error("Error al obtener la región seleccionada: " + e.getMessage());
            return null;
        }
        return regionPolygon;
    }
}
