package com.bteconosur.world.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.locationtech.jts.geom.Polygon;
import org.bukkit.Location;
import org.bukkit.World;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.RegionUtils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;


public class BTEWorld {

    private final CapaAlta capaAlta;
    private final CapaBaja capaBaja;
    private HashMap<UUID, BukkitTask> playerTasks = new HashMap<>();
    private HashMap<UUID, LabelWorld> lastLabelWorld = new HashMap<>();
    private boolean isValid = false;

    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

  
    public BTEWorld() {

        ConsoleLogger.info(lang.getString("bte-world-loading"));
        capaBaja = new CapaBaja(config.getString("layers.capa_baja.name"), config.getString("layers.capa_baja.display-name"), config.getInt("layers.capa_baja.offset"));
        capaAlta = new CapaAlta(config.getString("layers.capa_alta.name"), config.getString("layers.capa_alta.display-name"), config.getInt("layers.capa_alta.offset"));

        loadWorld();
        if (!isValid) ConsoleLogger.error("El mundo de BTE es inválido.");
    }

    private void loadWorld() {
        boolean ok = true;
        for (LabelWorld lw : List.of(capaAlta, capaBaja)) {
            if (lw.getBukkitWorld() == null) {
                ok = false;
                break;
            }
        }
        this.isValid = ok;
    }

    public LabelWorld getLabelWorld(double x, double z) {
        List<Polygon> regions = capaAlta.getRegions();
        for (Polygon p : regions) {
            if (RegionUtils.containsCoordinate(p, x, z)) return capaAlta;
        }
        
        return capaBaja;
    }

    public boolean isValidLocation(Location loc) {
        World bw = getLabelWorld(loc.getX(), loc.getZ()).getBukkitWorld();
        return bw == loc.getWorld();
    }

    public boolean isValidLocation(Location loc, LabelWorld lw) {
        World bw = lw.getBukkitWorld();
        return bw == loc.getWorld();
    }

    public void checkMove(Location lFrom, Location lTo, Player player) {
        LabelWorld currentlw = getLabelWorld(lTo.getX(), lTo.getZ());
        UUID pUuid = player.getUniqueId();
        if (isValidLocation(lTo) == false) return;
        LabelWorld lastlw = lastLabelWorld.get(pUuid);

        if (lastlw == null) {
            lastLabelWorld.put(pUuid, currentlw);
            return;
        }

        if (currentlw == lastlw && playerTasks.containsKey(pUuid)) {
            playerTasks.get(pUuid).cancel();      
            playerTasks.remove(pUuid);
            return;
        };
        if (currentlw == lastlw) return;

        int tpCooldownSeconds = config.getInt("tp-cooldown-seconds");
        if (lastlw != currentlw && !playerTasks.containsKey(pUuid)) {
            BukkitTask task = new BukkitRunnable() {
                Integer elapsedSeconds = 0;
                @Override
                public void run() {
                    if (!isValidLocation(player.getLocation())) {
                        playerTasks.remove(pUuid);
                        lastLabelWorld.remove(pUuid);
                        this.cancel();
                        return;
                    }
                    LabelWorld destination = getLabelWorld(player.getX(), player.getZ());
                    if (elapsedSeconds >= tpCooldownSeconds) {
                        this.cancel();
                        lastLabelWorld.put(pUuid, destination);
                        destination.teleportPlayer(player, player.getX(), convertY(player.getY(), lastlw, destination), player.getZ(), player.getYaw(), player.getPitch());
                        playerTasks.remove(pUuid);
                        return;
                    }
                    String titleText = lang.getString("teleport-layer-title").replace("%destination%", destination.getDisplayName());
                    String subtitleText = lang.getString("teleport-layer-subtitle").replace("%seconds%", String.valueOf(tpCooldownSeconds - elapsedSeconds));
                    Audience audience = player;
                    audience.showTitle(
                        Title.title(MiniMessage.miniMessage().deserialize(titleText), 
                        MiniMessage.miniMessage().deserialize(subtitleText),
                        Times.times(Duration.ofMillis(200), Duration.ofMillis(1000), Duration.ofMillis(200)))
                    );
                    elapsedSeconds += 1;
                }
            }.runTaskTimer(BTEConoSur.getInstance(), 0L, 20L);
            playerTasks.put(pUuid, task);
            return;
        }
    }

    private double convertY(double ySource, LabelWorld sourceLw, LabelWorld destLw) {
        if (sourceLw == null || destLw == null) return ySource;
        return ySource - destLw.getOffset() + sourceLw.getOffset();
    }

    public boolean isValid() {
        return this.isValid;
    }
    
    public void clearPlayerTasks(UUID pUuid) {
        if (playerTasks.containsKey(pUuid)) {
            playerTasks.get(pUuid).cancel();
            playerTasks.remove(pUuid);
            lastLabelWorld.remove(pUuid);
        }
    }

    public void shutdown() {
        playerTasks.values().forEach(task -> {
            if (!task.isCancelled()) task.cancel();
        });
        playerTasks.clear();
    }
}
