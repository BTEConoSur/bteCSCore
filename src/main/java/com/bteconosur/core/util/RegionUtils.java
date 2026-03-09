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
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;

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
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;


/**
 * Utilidad para manipulación de regiones poligonales y selección en WorldEdit.
 * Proporciona conversión entre geometrías JTS y regiones de WorldGuard/WorldEdit,
 * así como visualización de partículas y comparación de formas.
 */
public class RegionUtils {

    private static final GeometryFactory gf = new GeometryFactory();
    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    private static final Coordinate TMP_COORD = new Coordinate();

    /**
     * Selecciona un polígono en WorldEdit para un jugador.
     * Convierte la geometría a una selección poligonal 2D en modo WorldEdit.
     *
     * @param player jugador que recibe la selección.
     * @param poly polígono a seleccionar.
     * @param minY altura mínima de la selección.
     * @param maxY altura máxima de la selección.
     * @param language idioma para mensajes de error.
     */
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

    /**
     * Convierte un polígono JTS a una región poligonal protegida de WorldGuard.
     *
     * @param polygon polígono a convertir.
     * @param name nombre de la región protegida.
     * @return región protegida creada, o {@code null} si el polígono es nulo, vacío o inválido.
     */
    public static ProtectedPolygonalRegion toProtectedRegion(Polygon polygon, String name) {
        if (polygon == null || polygon.isEmpty()) return null;
        Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
        if (coords.length < 4) return null;

        List<BlockVector2> points = new ArrayList<>();
        for (int i = 0; i < coords.length - 1; i++) {
            int x = (int) Math.floor(coords[i].x);
            int z = (int) Math.floor(coords[i].y);
            points.add(BlockVector2.at(x, z));
        }
        return new ProtectedPolygonalRegion(name, points, config.getInt("min-height"), config.getInt("max-height"));
    }

    /**
     * Compara si un polígono de base de datos y una región protegida representan la misma forma.
     *
     * @param dbPoly polígono almacenado en base de datos.
     * @param region región protegida a comparar.
     * @return {@code true} si ambas geometrías son equivalentes tras normalización.
     */
    public static boolean sameShape(Polygon dbPoly, ProtectedPolygonalRegion region) {
        if (dbPoly == null || dbPoly.isEmpty() || region == null) return false;
        List<BlockVector2> pts = region.getPoints();
        Coordinate[] coords = new Coordinate[pts.size() + 1];
        for (int i = 0; i < pts.size(); i++) {
            coords[i] = new Coordinate(pts.get(i).getX(), pts.get(i).getZ());
        }
        coords[pts.size()] = coords[0];

        Polygon regionPoly = gf.createPolygon(coords);

        Polygon a = (Polygon) dbPoly.copy();
        Polygon b = (Polygon) regionPoly.copy();
        a.normalize();
        b.normalize();

        return a.equalsExact(b);
    }

    /**
     * Convierte una región cúbica de WorldEdit en un polígono 2D.
     *
     * @param region región cúbica a convertir.
     * @return polígono equivalente en el plano XZ.
     */
    public static Polygon toPolygon(CuboidRegion region) {
        return gf.createPolygon(new Coordinate[] {
            new Coordinate(region.getMinimumPoint().getX(), region.getMinimumPoint().getZ()),
            new Coordinate(region.getMinimumPoint().getX(), region.getMaximumPoint().getZ()),
            new Coordinate(region.getMaximumPoint().getX(), region.getMaximumPoint().getZ()),
            new Coordinate(region.getMaximumPoint().getX(), region.getMinimumPoint().getZ()),
            new Coordinate(region.getMinimumPoint().getX(), region.getMinimumPoint().getZ())
        });
    }

    /**
     * Convierte una región poligonal 2D de WorldEdit en un polígono JTS.
     *
     * @param region región poligonal a convertir.
     * @return polígono equivalente en el plano XZ.
     */
    public static Polygon toPolygon(Polygonal2DRegion region) {
        int size = region.getPoints().size();
        Coordinate[] coords = new Coordinate[size + 1];
        for (int i = 0; i < size; i++) {
            coords[i] = new Coordinate(region.getPoints().get(i).getX(), region.getPoints().get(i).getZ());
        }
        coords[size] = coords[0];
        return gf.createPolygon(coords);
    }

    /**
     * Obtiene la selección actual de WorldEdit del emisor y la convierte en polígono JTS.
     *
     * @param sender emisor del comando, que debe ser un jugador.
     * @return polígono de la selección actual, o {@code null} si no hay selección válida.
     */
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
            ConsoleLogger.error("Error al obtener la región seleccionada: ", e);
            return null;
        }
        return regionPolygon;
    }

    /**
     * Calcula el conjunto de chunks que intersectan con el polígono de un proyecto.
     *
     * @param proyecto proyecto del cual se toma el polígono.
     * @return conjunto de claves de chunk intersectadas, o conjunto vacío si no hay polígono.
     */
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

    /**
     * Determina si un polígono intersecta un chunk específico.
     *
     * @param poly polígono a evaluar.
     * @param chunkKey clave del chunk objetivo.
     * @return {@code true} si existe intersección entre el polígono y el chunk.
     */
    public static boolean intersectsChunk(Polygon poly, ChunkKey chunkKey) {
        return intersectsChunk(poly, chunkKey.x(), chunkKey.z());
    }

    /**
     * Determina si un polígono intersecta un chunk por sus coordenadas.
     *
     * @param poly polígono a evaluar.
     * @param chunkX coordenada X del chunk.
     * @param chunkZ coordenada Z del chunk.
     * @return {@code true} si existe intersección entre el polígono y el chunk.
     */
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

    /**
     * Verifica si una coordenada está contenida dentro de una geometría preparada,
     * validando previamente su envolvente para optimizar el cálculo.
     *
     * @param poly geometría preparada del polígono.
     * @param envelope envolvente del polígono.
     * @param x coordenada X a evaluar.
     * @param z coordenada Z a evaluar.
     * @return {@code true} si la coordenada pertenece a la geometría.
     */
    public static boolean containsCoordinate(PreparedGeometry poly, Envelope envelope, double x, double z) {
        if (poly == null) return false;
        if (!envelope.contains(x, z)) return false;
        TMP_COORD.setX(x);
        TMP_COORD.setY(z);
        return poly.covers(gf.createPoint(TMP_COORD));
    }

    /**
     * Dibuja partículas en el borde exterior e interior de un polígono sin desplazamiento.
     *
     * @param player jugador que verá las partículas.
     * @param poly polígono del cual se renderiza el borde.
     * @param particleName nombre de la partícula de Bukkit.
     */
    public static void spawnBorderParticles(Player player, Polygon poly, String particleName) {
        spawnBorderParticles(player, poly, particleName, 0.0);
    }

    /**
     * Dibuja partículas en el borde exterior e interior de un polígono con desplazamiento.
     *
     * @param player jugador que verá las partículas.
     * @param poly polígono del cual se renderiza el borde.
     * @param particleName nombre de la partícula de Bukkit.
     * @param offset desplazamiento aplicado en los ejes X y Z al renderizado.
     */
    public static void spawnBorderParticles(Player player, Polygon poly, String particleName, double offset) {
        if (poly == null || poly.isEmpty()) return;
        
        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            particle = Particle.FLAME;
        }
        
        CoordinateSequence seq = poly.getExteriorRing().getCoordinateSequence();
        spawnBorderParticles(player, seq, particle, offset);
        int cantInterior = poly.getNumInteriorRing();
        if (cantInterior > 0) {
            for (int i = 0; i < cantInterior; i++) {
                spawnBorderParticles(player, poly.getInteriorRingN(i).getCoordinateSequence(), particle, offset);
            }
        }
    }

    /**
     * Dibuja partículas a lo largo de una secuencia de coordenadas con límites de distancia
     * y cantidad de capas configuradas.
     *
     * @param player jugador que verá las partículas.
     * @param seq secuencia de coordenadas que define el borde.
     * @param particle partícula a renderizar.
     * @param offset desplazamiento aplicado en los ejes X y Z al renderizado.
     */
    public static void spawnBorderParticles(Player player, CoordinateSequence seq, Particle particle, double offset) {
        double playerX = player.getLocation().getX();
        double playerZ = player.getLocation().getZ();
        double playerY = player.getLocation().getY();
        double maxDistance = config.getDouble("border-particles.max-distance");
        int layers = config.getInt("border-particles.layers");
        double maxDistSq = maxDistance * maxDistance;
        double height = playerY - (layers / 2.0);
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
