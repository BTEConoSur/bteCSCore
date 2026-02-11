package com.bteconosur.world;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.ChunkKey;
import com.bteconosur.world.listener.WorldEditListener;
import com.bteconosur.world.model.BTEWorld;
import com.bteconosur.world.model.LabelWorld;
import com.sk89q.worldedit.WorldEdit;

import java.util.Set;

import org.bukkit.Location;

public class WorldManager {

    private static WorldManager instance;

    private final YamlConfiguration lang;

    private BTEWorld bteWorld;

    public WorldManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();

        ConsoleLogger.info(lang.getString("world-module-initializing"));

        bteWorld = new BTEWorld();
        WorldEdit worldEdit = BTEConoSur.getWorldEditPlugin().getWorldEdit();
        if (worldEdit != null) {
            worldEdit.getEventBus().register(new WorldEditListener(this));
        }
    }

    public boolean canBuild(Location loc, Player player) {
        if (loc.getWorld().getName().equalsIgnoreCase("lobby")) return true; // Delego en WorldGuard
        if (bteWorld == null || !bteWorld.isValid()) {
            //ConsoleLogger.debug("[WorldManager] canBuild false: bteWorld null o inválido");
            return false;
        }

        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null) {
            //ConsoleLogger.debug("[WorldManager] canBuild false: bukkitPlayer null");
            return false;
        }
        if (bukkitPlayer.hasPermission("btecs.world.bypass")) {
            //ConsoleLogger.debug("[WorldManager] canBuild true: Permiso bypass");
            return true;
        }

        //if (!bukkitPlayer.hasPermission("btecs.world.build") && !bukkitPlayer.hasPermission("btecs.world.select")) {
        //   ConsoleLogger.debug("[WorldManager] canBuild false: No permiso build");
        //    return false;
        //}

        LabelWorld lw = bteWorld.getLabelWorld(loc.getX(), loc.getZ());
        if (lw == null) {
            //ConsoleLogger.debug("[WorldManager] canBuild false: LabelWorld no encontrada para (" + loc.getX() + ", " + loc.getZ() + ")");
            return false;
        }
        if (!bteWorld.isValidLocation(loc, lw)) {
            //ConsoleLogger.debug("[WorldManager] canBuild false: Ubicación inválida para LabelWorld " + lw.getName());
            return false;
        }

        ChunkKey chunkKey = ChunkKey.fromBlock(loc.blockX(), loc.blockZ());
        Pais pais = PaisRegistry.getInstance().findByLocation(loc.getBlockX(), loc.getBlockZ());
        if (pais == null) {
            //ConsoleLogger.debug("[WorldManager] canBuild false: País no encontrado en la ubicación (" + loc.getX() + ", " + loc.getZ() + ")");
            return false;
        }

        PermissionManager pm = PermissionManager.getInstance();
        if (pm.isManager(player, pais)) {
            //ConsoleLogger.debug("[WorldManager] canBuild true: Es manager del país " + pais.getNombre());
            return true;
        }

        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Set<Proyecto> proyectos = pr.getByLocation(loc.getBlockX(), loc.getBlockZ(), pr.getByChunk(chunkKey));

        if (proyectos == null || proyectos.isEmpty()) {
            //ConsoleLogger.debug("[WorldManager] canBuild false: No hay proyectos en la ubicación (" + loc.getX() + ", " + loc.getZ() + ")");
            return false;
        }

        if (!pm.areActiveOrEditing(proyectos)) {
            //ConsoleLogger.debug("[WorldManager] canBuild false: No hay proyectos activos o en edición en la ubicación (" + loc.getX() + ", " + loc.getZ() + ")");
            return false;
        }

        if (pm.isReviewer(player, pais)) {
            //ConsoleLogger.debug("[WorldManager] canBuild true: Es reviewer del país " + pais.getNombre());
            return true;
        }

        if (pm.isMiembro(player, proyectos) || pm.isLider(player, proyectos)) {
            //ConsoleLogger.debug("[WorldManager] canBuild true: Tiene permisos en los proyectos de la ubicación (" + loc.getX() + ", " + loc.getZ() + ")");
            return true;
        }

        //ConsoleLogger.debug("[WorldManager] canBuild false: No permitido en " + lw.getName());
        return false;
    }

    public void checkLayerMove(Location lFrom, Location lTo, org.bukkit.entity.Player player) {
        if (bteWorld == null || !bteWorld.isValid()) return;
        if (lTo.getWorld().getName().equalsIgnoreCase("lobby")) return;
        bteWorld.checkLayerMove(lFrom, lTo, player);
    }

    public boolean checkPaisMove(Location lFrom, Location lTo, org.bukkit.entity.Player player) {
        if (lTo.getWorld().getName().equalsIgnoreCase("lobby")) return true;
        return bteWorld.checkPaisMove(lFrom, lTo, player);
    }

    public BTEWorld getBTEWorld() {
        return this.bteWorld;
    }


    public void shutdown() {
        ConsoleLogger.info(lang.getString("world-module-shutting-down"));
        bteWorld.shutdown();
    }

    public static WorldManager getInstance() {
        if (instance == null) {
            instance = new WorldManager();
        }
        return instance;
    }

}
