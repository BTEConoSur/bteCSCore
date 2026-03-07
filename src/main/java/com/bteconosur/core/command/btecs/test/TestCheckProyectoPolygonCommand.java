package com.bteconosur.core.command.btecs.test;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Proyecto;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class TestCheckProyectoPolygonCommand extends BaseCommand {

    private static final GeometryFactory gf = new GeometryFactory();

    public TestCheckProyectoPolygonCommand() {
        super("checkpolygon", "", "btecs.command.btecs.test", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
        String lobbyWorldName = ConfigHandler.getInstance().getConfig().getString("lobby.world");
        String prefix = "project_";

        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        if (lobbyWorld == null) {
            PlayerLogger.error(player, "No se encontró el mundo '" + lobbyWorldName + "'.", (String) null);
            return true;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(lobbyWorld));
        if (regionManager == null) {
            PlayerLogger.error(player, "No se pudo obtener el RegionManager del mundo '" + lobbyWorldName + "'.", (String) null);
            return true;
        }

        DBManager dbManager = DBManager.getInstance();
        List<Proyecto> proyectos = dbManager.selectAll(Proyecto.class);
        int found = 0;
        int total = 0;

        PlayerLogger.info(player, "Iniciando checkpolygon para " + proyectos.size() + " proyectos...", (String) null);

        for (Proyecto proyecto : proyectos) {
            if (proyecto.getPoligono() != null) continue;
            total++;

            String regionName = prefix + proyecto.getId();
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region == null) {
                region = regionManager.getRegion(regionName.toLowerCase());
            }

            if (region instanceof ProtectedPolygonalRegion polyRegion) {
                List<BlockVector2> pts = polyRegion.getPoints();
                if (pts.size() < 3) continue;

                Coordinate[] coords = new Coordinate[pts.size() + 1];
                for (int i = 0; i < pts.size(); i++) {
                    coords[i] = new Coordinate(pts.get(i).getX(), pts.get(i).getZ());
                }
                coords[pts.size()] = coords[0];

                Polygon polygon = gf.createPolygon(coords);
                proyecto.setPoligono(polygon);
                dbManager.merge(proyecto);

                found++;
            }
        }

        PlayerLogger.info(player, "Proceso completado: " + found + " polígonos cargados de " + total + " proyectos sin polígono.", (String) null);
        return true;
    }

}
