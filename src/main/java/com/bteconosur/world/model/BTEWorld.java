package com.bteconosur.world.model;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location;
import org.bukkit.World;
import org.locationtech.jts.geom.Coordinate;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;


public class BTEWorld {

    private final List<LabelWorld> labelWorlds = new ArrayList<>();
    private boolean isValid = false;

    private ConsoleLogger logger = BTEConoSur.getConsoleLogger();
    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

  
    public BTEWorld() {

        logger.info(lang.getString("bte-world-loading"));
        labelWorlds.add(new LabelWorld("capa_1", 0));
        labelWorlds.add(new LabelWorld("capa_2", 4064));

        loadWorld();

        if (!isValid) logger.error("El mundo de BTE es inválido.");
    }

    private void loadWorld() {
        boolean ok = true;
        for (LabelWorld lw : labelWorlds) {
            if (lw.getRegion() == null || lw.getBukkitWorld() == null) {
                ok = false;
                break;
            }
        }
        this.isValid = ok;
    }

    public LabelWorld getLabelWorld(double x, double z) {
        for (LabelWorld lw : labelWorlds) {
            if (lw.getRegion().contains(lw.getRegion().getFactory().createPoint(new Coordinate(x, z)))) {
                return lw;
            }
        }
        return null;
    }

    public boolean isValidLocation(Location loc) {
        World bw = getLabelWorld(loc.getX(), loc.getZ()).getBukkitWorld();
        return bw == loc.getWorld();
    }

    public boolean isValidLocation(Location loc, LabelWorld lw) {
        World bw = lw.getBukkitWorld();
        return bw == loc.getWorld();
    }

    public boolean isValid() {
        return this.isValid;
    }

    public List<LabelWorld> getLabelWorlds() {
        return this.labelWorlds;
    }
}
