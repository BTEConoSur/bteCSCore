package com.bteconosur.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.ChunkKey;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;


public class RegionUtils {

    private static final GeometryFactory gf = new GeometryFactory();
    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static void selectPolygon(Player player, Polygon poly, int minY, int maxY, Language language) {
        
        if (poly == null || poly.isEmpty()) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "internal-error"), (String) null);
            return;
        }

        Coordinate[] coords = poly.getExteriorRing().getCoordinates();
        if (coords.length < 4) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "internal-error"), (String) null);
            return;
        }

        List<BlockVector2> points = new ArrayList<>();
        for (int i = 0; i < coords.length - 1; i++) {
            int x = (int) Math.floor(coords[i].x);
            int z = (int) Math.floor(coords[i].y);
            points.add(BlockVector2.at(x, z));
        }

        var weWorld = BukkitAdapter.adapt(player.getWorld());
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
        Polygonal2DRegionSelector selector = new Polygonal2DRegionSelector(weWorld, points, minY, maxY);
        session.setRegionSelector(weWorld, selector);
        session.dispatchCUISelection(BukkitAdapter.adapt(player));
        PlayerLogger.info(player, LanguageHandler.getText(language, "region.selected"), (String) null);
    }

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
        Language language = PlayerRegistry.getInstance().get(sender).getLanguage();
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
                    PlayerLogger.error(player, LanguageHandler.getText(language, "region.not-complete"), (String) null);
                    return null;
                }
                regionPolygon = toPolygon(poly);
            } else {
                PlayerLogger.error(player, LanguageHandler.getText(language, "region.not-supported"), (String) null);
                return null;
            }
        } catch (IncompleteRegionException e) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "region.not-selected"), (String) null);
            return null;
        } catch (Exception e) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "region.we-error"), (String) null);
            ConsoleLogger.error("Error al obtener la región seleccionada: " + e.getMessage());
            return null;
        }
        return regionPolygon;
    }

    public static Set<ChunkKey> chunksFor(Proyecto proyecto) {
        Polygon poly = proyecto.getPoligono();
        if (poly == null || poly.isEmpty()) return Set.of();

        var env = poly.getEnvelopeInternal();
        int minChunkX = Math.floorDiv((int) Math.floor(env.getMinX()), 16);
        int maxChunkX = Math.floorDiv((int) Math.floor(env.getMaxX()), 16);
        int minChunkZ = Math.floorDiv((int) Math.floor(env.getMinY()), 16);
        int maxChunkZ = Math.floorDiv((int) Math.floor(env.getMaxY()), 16);

        Set<ChunkKey> result = new HashSet<>();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (intersectsChunk(poly, cx, cz)) result.add(ChunkKey.of(cx, cz));
            }
        }
        return result;
    }

    public static boolean intersectsChunk(Polygon poly, ChunkKey chunkKey) {
        return intersectsChunk(poly, chunkKey.x(), chunkKey.z());
    }

    private static boolean intersectsChunk(Polygon poly, int chunkX, int chunkZ) {
        if (poly == null || poly.isEmpty()) return false;
        double x0 = chunkX * 16.0;
        double z0 = chunkZ * 16.0;
        double x1 = x0 + 16.0;
        double z1 = z0 + 16.0;
        var geomFactory = poly.getFactory();
        var ring = geomFactory.createLinearRing(new Coordinate[]{
            new Coordinate(x0, z0), new Coordinate(x1, z0),
            new Coordinate(x1, z1), new Coordinate(x0, z1),
            new Coordinate(x0, z0)
        });
        var chunkPoly = geomFactory.createPolygon(ring, null);
        return poly.intersects(chunkPoly);
    }

    public static boolean containsCoordinate(Polygon poly, double x, double z) {
        if (poly == null || poly.isEmpty()) return false;
        return poly.covers(gf.createPoint(new Coordinate(x, z)));
    }

    public static void spawnBorderParticles(Player player, Polygon poly, String particleName) {
        if (poly == null || poly.isEmpty()) return;
        
        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            particle = Particle.FLAME;
        }
        
        double maxDistance = config.getDouble("border-particles.max-distance");
        double maxDistSq = maxDistance * maxDistance;
        int layers = config.getInt("border-particles.layers");
        double playerY = player.getLocation().getY();
        double height = playerY - (layers / 2.0);
        
        double playerX = player.getLocation().getX();
        double playerZ = player.getLocation().getZ();
        
        CoordinateSequence seq = poly.getExteriorRing().getCoordinateSequence();
        int size = seq.size();
        
        for (int i = 0; i < size - 1; i++) {
            double startX = seq.getX(i);
            double startZ = seq.getY(i);
            double endX = seq.getX(i + 1);
            double endZ = seq.getY(i + 1);
            
            double minX = Math.min(startX, endX) - maxDistance;
            double maxX = Math.max(startX, endX) + maxDistance;
            double minZ = Math.min(startZ, endZ) - maxDistance;
            double maxZ = Math.max(startZ, endZ) + maxDistance;
            
            if (playerX < minX || playerX > maxX || playerZ < minZ || playerZ > maxZ) {
                continue;
            }
            
            double segDx = endX - startX;
            double segDz = endZ - startZ;
            double segmentLength = Math.sqrt(segDx * segDx + segDz * segDz);
            int numPoints = (int) Math.ceil(segmentLength);
            if (numPoints < 1) numPoints = 1;
            
            for (int j = 0; j < numPoints; j++) {
                double t = j / (double) numPoints;
                double x = startX + segDx * t;
                double z = startZ + segDz * t;
                
                double dx = x - playerX;
                double dz = z - playerZ;
                double distSq = dx * dx + dz * dz;
                
                if (distSq <= maxDistSq) {
                    for (int layer = 0; layer < layers; layer++) {
                        player.spawnParticle(particle, x, height + layer, z, 1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }

    public static void spawnBorderParticles(Player player, Polygon poly, String particleName, double offset) {
        if (poly == null || poly.isEmpty()) return;
        
        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            particle = Particle.FLAME;
        }
        
        double maxDistance = config.getDouble("border-particles.max-distance");
        double maxDistSq = maxDistance * maxDistance;
        int layers = config.getInt("border-particles.layers");
        double playerY = player.getLocation().getY();
        double height = playerY - (layers / 2.0);
        
        double playerX = player.getLocation().getX();
        double playerZ = player.getLocation().getZ();
        
        CoordinateSequence seq = poly.getExteriorRing().getCoordinateSequence();
        int size = seq.size();
        
        for (int i = 0; i < size - 1; i++) {
            double startX = seq.getX(i);
            double startZ = seq.getY(i);
            double endX = seq.getX(i + 1);
            double endZ = seq.getY(i + 1);
            
            double minX = Math.min(startX, endX) - maxDistance;
            double maxX = Math.max(startX, endX) + maxDistance;
            double minZ = Math.min(startZ, endZ) - maxDistance;
            double maxZ = Math.max(startZ, endZ) + maxDistance;
            
            if (playerX < minX || playerX > maxX || playerZ < minZ || playerZ > maxZ) {
                continue;
            }
            
            double segDx = endX - startX;
            double segDz = endZ - startZ;
            double segmentLength = Math.sqrt(segDx * segDx + segDz * segDz);
            int numPoints = (int) Math.ceil(segmentLength);
            if (numPoints < 1) numPoints = 1;
            
            for (int j = 0; j < numPoints; j++) {
                double t = j / (double) numPoints;
                double x = startX + segDx * t;
                double z = startZ + segDz * t;
                
                double dx = x - playerX;
                double dz = z - playerZ;
                double distSq = dx * dx + dz * dz;
                
                if (distSq <= maxDistSq) {
                    for (int layer = 0; layer < layers; layer++) {
                        player.spawnParticle(particle, x + offset, height + layer, z + offset, 1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }

}
