package com.bteconosur.world;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.world.model.BTEWorld;
import com.bteconosur.world.model.LabelWorld;

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

    }

    public boolean canBuild(Location loc, Player player) {
        if (bteWorld == null || !bteWorld.isValid()) return false;

        // Verificar que es Admin

        if (loc.getWorld().getName().equalsIgnoreCase("lobby")) return true; // Delego en WorldGuard
        
        LabelWorld lw = bteWorld.getLabelWorld(loc.getX(), loc.getZ());
        logger.debug("LabelWorld: " + lw);       
        if (lw == null) return false;
        logger.debug("Is valid location: " + bteWorld.isValidLocation(loc, lw));  
        if (!bteWorld.isValidLocation(loc, lw)) return false;
        logger.debug("Player: " + player);
        // TODO: Obtener país de la location
        // TODO: Verificar que es manager de este pais

        // TODO: Obtener los proyectos en esa location
        // TODO: Verificar que no sean null

        // TODO: Verificar que sea reviewer de ese pais
        // TODO: Verificar que sea miembro de algun proyecto 

        return true;
    }

    public BTEWorld getBTEWorld() {
        return this.bteWorld;
    }


    public void shutdown() {
        logger.info(lang.getString("world-module-shutting-down"));
    }

}
