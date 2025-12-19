package com.bteconosur.world;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.world.listener.WorldEditListener;
import com.bteconosur.world.model.BTEWorld;
import com.bteconosur.world.model.LabelWorld;
import com.sk89q.worldedit.WorldEdit;

import org.bukkit.Location;

public class WorldManager {

    private final YamlConfiguration lang;
    private final YamlConfiguration config;
    private final ConsoleLogger logger;

    private BTEWorld bteWorld;

    public WorldManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        config = configHandler.getConfig();
        logger = BTEConoSur.getConsoleLogger();

        logger.info(lang.getString("world-module-initializing"));

        bteWorld = new BTEWorld();
        WorldEdit worldEdit = BTEConoSur.getWorldEditPlugin().getWorldEdit();
        if (worldEdit != null) {
            worldEdit.getEventBus().register(new WorldEditListener(this));
        }
    }

    public boolean canBuild(Location loc, Player player) {
        if (bteWorld == null || !bteWorld.isValid()) {
            logger.debug("[WorldManager] canBuild false: bteWorld null o inválido");
            return false;
        }

        // TODO: Verificar que es Admin

        if (loc.getWorld().getName().equalsIgnoreCase("lobby")) return true; // Delego en WorldGuard
        LabelWorld lw = bteWorld.getLabelWorld(loc.getX(), loc.getZ());
        if (lw == null) {
            logger.debug("[WorldManager] canBuild false: LabelWorld no encontrada para (" + loc.getX() + ", " + loc.getZ() + ")");
            return false;
        }
        if (!bteWorld.isValidLocation(loc, lw)) {
            logger.debug("[WorldManager] canBuild false: Ubicación inválida para LabelWorld " + lw.getName());
            return false;
        }

        // TODO: Obtener país de la location
        // TODO: Verificar que es manager de este pais

        // TODO: Obtener los proyectos en esa location
        // TODO: Verificar que no sean null

        // TODO: Verificar que sea reviewer de ese pais
        // TODO: Verificar que sea miembro de algun proyecto 
        logger.debug("[WorldManager] canBuild true: Permitido en " + lw.getName());
        return true;
    }

    public void checkMove(Location lFrom, Location lTo, org.bukkit.entity.Player player) {
        if (bteWorld == null || !bteWorld.isValid()) return;
        if (lTo.getWorld().getName().equalsIgnoreCase("lobby")) return;
        bteWorld.checkMove(lFrom, lTo, player);
    }

    public BTEWorld getBTEWorld() {
        return this.bteWorld;
    }


    public void shutdown() {
        logger.info(lang.getString("world-module-shutting-down"));
        bteWorld.shutdown();
    }

}
