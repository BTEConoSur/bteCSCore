package com.bteconosur.world.model;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

public abstract class LabelWorld {

    private final String name;
    private final String displayName;
    private final int offset;

    private final World bukkitWorld;

    public LabelWorld(String name, String displayName, int offset) {
        String msg = LanguageHandler.getText("label-world-loading").replace("%name%", name).replace("%offset%", String.valueOf(offset));
        ConsoleLogger.info(msg);

        this.name = name;
        this.displayName = displayName;
        this.offset = offset;

        bukkitWorld = BTEConoSur.getInstance().getServer().getWorld(name);
        if (bukkitWorld == null) ConsoleLogger.error(LanguageHandler.getText("invalid-label-world").replace("%name%", name));
    }

    public void teleportPlayer(Player player, double x, double y, double z, float yaw, float pitch) {
        if (bukkitWorld == null) return;
        player.teleport(new Location(bukkitWorld, x, y, z, yaw, pitch));
    }

    public boolean isValidLocation(Location location) {
        if (location == null) return false;
        if (bukkitWorld == null) return false;
        return location.getWorld().equals(bukkitWorld);
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
