package com.bteconosur.world.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.locationtech.jts.geom.Coordinate;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PlayerLogger;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;


public class BTEWorld {

    private final List<LabelWorld> labelWorlds = new ArrayList<>();
    private HashMap<UUID, BukkitTask> playerTasks = new HashMap<>();
    private HashMap<UUID, LabelWorld> lastLabelWorld = new HashMap<>();
    private boolean isValid = false;

    private ConsoleLogger logger = BTEConoSur.getConsoleLogger();
    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    private final MultiverseCoreApi multiverseApi = BTEConoSur.getMultiverseCoreApi();

    private final Integer tpCooldownSeconds;
    private final String teleportTitle;
    private final String teleportSubtitle;
  
    public BTEWorld() {

        logger.info(lang.getString("bte-world-loading"));
        labelWorlds.add(new LabelWorld("capa_1", "Capa 1", config.getInt("layer-1-offset")));
        labelWorlds.add(new LabelWorld("capa_2", "Capa 2", config.getInt("layer-2-offset")));

        tpCooldownSeconds = config.getInt("tp-cooldown-seconds");
        teleportTitle = lang.getString("teleport-title");
        teleportSubtitle = lang.getString("teleport-subtitle");

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

    public void checkMove(Location lFrom, Location lTo, Player player) {
        LabelWorld currentlw = getLabelWorld(lTo.getX(), lTo.getZ());
        UUID pUuid = player.getUniqueId();
        
        LabelWorld lastlw = lastLabelWorld.get(pUuid);
        if (currentlw == null && lastlw == null) {
            PlayerLogger.warn(com.bteconosur.db.model.Player.getBTECSPlayer(player), lang.getString("not-limbo"), (String) null);
            player.teleport(multiverseApi.getWorldManager().getLoadedWorld("lobby").get().getSpawnLocation());
            return;
        };

        if (lastlw == null && currentlw != null) {
            lastLabelWorld.put(pUuid, currentlw);
            return;
        }  //TODO: Chequear casos que se quedan en el limbo

        if (currentlw == lastlw && playerTasks.containsKey(pUuid)) {
            playerTasks.get(pUuid).cancel();      
            playerTasks.remove(pUuid);
            return;
        };
        if (currentlw == lastlw) return;

        if ((currentlw == null || lastlw != currentlw) && !playerTasks.containsKey(pUuid) && lastlw != null) {
            BukkitTask task = new BukkitRunnable() {
                Integer elapsedSeconds = 0;
                @Override
                public void run() {
                    LabelWorld currentlw2 = getLabelWorld(player.getX(), player.getZ());
                    LabelWorld destination = currentlw2 != null ? currentlw2 : lastlw;
                    if (elapsedSeconds >= tpCooldownSeconds) {
                        this.cancel();
                        if (currentlw2 != null) {
                            lastLabelWorld.put(pUuid, destination);
                            destination.teleportPlayer(player, player.getX(), convertY(player.getY(), lastlw, destination), player.getZ(), player.getYaw(), player.getPitch());
                        }
                        else {
                            destination.teleportPlayer(player, lFrom.getX(), lFrom.getY(), lFrom.getZ(), player.getYaw(), player.getPitch());
                        }
                        playerTasks.remove(pUuid);
                        return;
                    }
                    String titleText = teleportTitle.replace("%destination%", destination.getDisplayName());
                    String subtitleText = teleportSubtitle.replace("%seconds%", String.valueOf(tpCooldownSeconds - elapsedSeconds));
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

    public List<LabelWorld> getLabelWorlds() {
        return this.labelWorlds;
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
