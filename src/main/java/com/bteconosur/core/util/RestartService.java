package com.bteconosur.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.s;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.chat.GlobalChatService;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Gestiona reinicios automáticos y forzados del servidor.
 */
public class RestartService {

    private static RestartService instance;

    private final BTEConoSur plugin;
    private final YamlConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    private BukkitTask restartTask;
    private BukkitTask warningTask;
    private long restartAtMillis = -1L;

    private boolean isRestarting = false;
    private boolean sentInfoMessage = false;

    private RestartService() {
        plugin = BTEConoSur.getInstance();
        config = ConfigHandler.getInstance().getConfig();
        reloadFromConfig();
    }

    public static RestartService getInstance() {
        if (instance == null) {
            instance = new RestartService();
        }
        return instance;
    }

    public void reloadFromConfig() {
        cancelScheduledRestart(false);

        if (!config.getBoolean("restart-enabled", true)) {
            return;
        }

        long delayMinutes = config.getLong("restart-delay-minutes", 0L);
        scheduleAutomaticRestart(delayMinutes);
    }

    public void scheduleAutomaticRestart(long delayMinutes) {
        scheduleRestart(delayMinutes, true);
    }

    public void forceRestart(long delayMinutes) {
        scheduleRestart(delayMinutes, false);
    }

    public boolean cancelRestart() {
        return cancelScheduledRestart(false);
    }

    public boolean hasScheduledRestart() {
        return restartAtMillis > 0L;
    }

    public long getRemainingMillis() {
        if (!hasScheduledRestart()) {
            return -1L;
        }

        return Math.max(0L, restartAtMillis - System.currentTimeMillis());
    }

    public String getRemainingTimeText(Language language) {
        long remainingMillis = getRemainingMillis();
        if (remainingMillis < 0L) {
            return null;
        }

        return formatTime(remainingMillis, language);
    }

    public String getStatusMessage(Language language) {
        String remainingTime = getRemainingTimeText(language);
        if (remainingTime == null) {
            return LanguageHandler.getText(language, "restart-not-scheduled");
        }

        return LanguageHandler.getText(language, "restart-status").replace("%time%", remainingTime);
    }

    public void shutdown() {
        cancelScheduledRestart(false);
        clearBossBars();
        restartAtMillis = -1L;
        if (instance == this) {
            instance = null;
        }
    }

    private void scheduleRestart(long delayMinutes, boolean logToConsole) {
        if (delayMinutes < 0L) {
            delayMinutes = 0L;
        }

        cancelScheduledRestart(false);

        restartAtMillis = System.currentTimeMillis() + (delayMinutes * 60_000L);
        long delayTicks = Math.max(1L, delayMinutes * 60L * 20L);
        restartTask = Bukkit.getScheduler().runTaskLater(plugin, this::executeRestart, delayTicks);

        if (config.getLong("restart-warning-minutes", 0L) > 0L) {
            warningTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateWarningBossBars, 0L, 20L);
        }

        if (logToConsole) {
            ConsoleLogger.info(LanguageHandler.getText("restart-scheduled").replace("%time%", formatTime(delayMinutes * 60_000L, Language.getDefault())));
        }
    }

    private boolean cancelScheduledRestart(boolean logToConsole) {
        boolean hadRestart = hasScheduledRestart();

        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }

        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }

        clearBossBars();
        restartAtMillis = -1L;
        sentInfoMessage = false;
        if (hadRestart && logToConsole) {
            ConsoleLogger.info(LanguageHandler.getText("restart-cancelled"));
        }

        return hadRestart;
    }

    private void updateWarningBossBars() {
        long remainingMillis = getRemainingMillis();
        long warningMillis = config.getLong("restart-warning-minutes", 0L) * 60_000L;

        if (remainingMillis <= 0L || warningMillis <= 0L || remainingMillis > warningMillis) {
            clearBossBars();
            return;
        }

        if (!sentInfoMessage && config.getBoolean("discord-server-restart-info")) {
            GlobalChatService.broadcastEmbed(ChatUtil.getServerRestartInfo());
            sentInfoMessage = true;
        }

        float progress = Math.max(0.0F, Math.min(1.0F, remainingMillis / (float) warningMillis));
        Map<UUID, BossBar> visibleBars = new HashMap<>();

        for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
            org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
            Language language = player != null ? player.getLanguage() : Language.getDefault();
            String title = LanguageHandler.getText(language, "restart-bossbar").replace("%time%", formatBossBarTime(remainingMillis));
            BossBar bossBar = bossBars.computeIfAbsent(bukkitPlayer.getUniqueId(), uuid -> BossBar.bossBar(
                    miniMessage.deserialize(title),
                    progress,
                    BossBar.Color.RED,
                    BossBar.Overlay.PROGRESS));

            bossBar.name(miniMessage.deserialize(title));
            bossBar.progress(progress);
            bukkitPlayer.showBossBar(bossBar);
            visibleBars.put(bukkitPlayer.getUniqueId(), bossBar);
        }

        for (Entry<UUID, BossBar> entry : new HashMap<>(bossBars).entrySet()) {
            if (visibleBars.containsKey(entry.getKey())) {
                continue;
            }

            org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(entry.getKey());
            if (onlinePlayer != null) {
                onlinePlayer.hideBossBar(entry.getValue());
            }
            bossBars.remove(entry.getKey());
        }
    }

    private void clearBossBars() {
        for (Entry<UUID, BossBar> entry : bossBars.entrySet()) {
            org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(entry.getKey());
            if (onlinePlayer != null) {
                onlinePlayer.hideBossBar(entry.getValue());
            }
        }

        bossBars.clear();
    }

    private void executeRestart() {
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }

        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }
        isRestarting = true;
        restartAtMillis = -1L;
        kickAllPlayers();
        if (config.getBoolean("discord-server-restart")) GlobalChatService.broadcastEmbed(ChatUtil.getServerRestarted());
        clearBossBars();
        Bukkit.restart();
    }

    private void kickAllPlayers() {
        for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
            org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
            Language language = player != null ? player.getLanguage() : Language.getDefault();
            Component message = miniMessage.deserialize(LanguageHandler.getText(language, "restart-kick"));
            bukkitPlayer.kick(message);
        }
    }

    private String formatTime(long millis, Language language) {
        double seconds = millis / 1000.0D;

        if (seconds < 60.0D) {
            return String.format(LanguageHandler.getText(language, "command-cooldown-second-format"), seconds);
        } else if (seconds >= 3600.0D) {
            long hours = (long) (seconds / 3600.0D);
            double remainingMinutes = (seconds % 3600.0D) / 60.0D;
            return String.format(LanguageHandler.getText(language, "command-cooldown-hour-format"), hours, remainingMinutes);
        }

        long minutes = (long) (seconds / 60.0D);
        double remainingSeconds = seconds % 60.0D;
        return String.format(LanguageHandler.getText(language, "command-cooldown-minute-format"), minutes, remainingSeconds);
    }

    private String formatBossBarTime(long millis) {
        long totalSeconds = Math.max(1L, (long) Math.ceil(millis / 1000.0D));

        if (totalSeconds < 60L) {
            return totalSeconds + "s";
        }

        if (totalSeconds < 3600L) {
            long minutes = totalSeconds / 60L;
            long seconds = totalSeconds % 60L;
            return minutes + "m " + seconds + "s";
        }

        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        return hours + "h " + minutes + "m";
    }

    public boolean isRestarting() {
        return isRestarting;
    }
}