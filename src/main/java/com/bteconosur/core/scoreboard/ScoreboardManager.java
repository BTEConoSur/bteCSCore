package com.bteconosur.core.scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;

public class ScoreboardManager {
    
    private static ScoreboardManager instance;
    private ScoreboardLibrary scoreboardLibrary;
    private final BTEConoSur plugin;

    private Map<UUID, Sidebar> playerScoreboards = new HashMap<>();
    private List<Scoreboard> scoreboards = List.of(
        new OnlineScoreboard(),
        new PlayerScoreboard(),
        new ProyectoScoreboard()
    );

    private BukkitTask rotationTask;
    private BukkitTask refreshTask;

    private int globalIndex = 0;
    private Scoreboard currentScoreboard;

    private final YamlConfiguration config;
    
    public ScoreboardManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        plugin = BTEConoSur.getInstance();
        ConsoleLogger.info(LanguageHandler.getText("scoreboard-manager-initializing"));
        try {
            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(plugin);
        } catch (NoPacketAdapterAvailableException e) {
            scoreboardLibrary = new NoopScoreboardLibrary();
            ConsoleLogger.error(LanguageHandler.getText("scoreboard-manager-error"), e);
        }
        startRotation(config.getInt("scoreboard-rotation"), config.getInt("proyecto-scoreboard-refresh"));
    }

    public Sidebar addPlayer(org.bukkit.entity.Player bukkitPlayer) {
        Sidebar sidebar = scoreboardLibrary.createSidebar();
        sidebar.addPlayer(bukkitPlayer);
        playerScoreboards.put(bukkitPlayer.getUniqueId(), sidebar);
        return sidebar;
    }

    public void removePlayer(org.bukkit.entity.Player bukkitPlayer) {
        Sidebar sidebar = playerScoreboards.remove(bukkitPlayer.getUniqueId());
        if (sidebar != null) {
            sidebar.removePlayer(bukkitPlayer);
            sidebar.close();
        }
    }

    public void startRotation(int seconds, int proyectoRefreshSeconds) {
        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            globalIndex = (globalIndex + 1) % scoreboards.size();
            currentScoreboard = scoreboards.get(globalIndex);
            renderAll();
            restartRefreshTask();
        }, 0L, seconds * 20L);
    }

    private void restartRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        if (!currentScoreboard.isRefreshable()) return;
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
                render(player, currentScoreboard);
            }

        }, 0L, currentScoreboard.getRefreshIntervalTicks());
    }

    private void renderAll() {
        if (currentScoreboard == null) return;

        if (currentScoreboard.isGlobal()) {
            currentScoreboard.update();
        }

        for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
            render(player, currentScoreboard);
        }
    }

    private void render(Player player, Scoreboard scoreboard) {
        Sidebar sidebar = playerScoreboards.get(player.getUuid());
        if (sidebar == null) {
            sidebar = addPlayer(player.getBukkitPlayer());
        }
        ComponentSidebarLayout layout = scoreboard.getLayout(player, player.getLanguage());
        layout.apply(sidebar);
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("scoreboard-manager-shutting-down"));
        if (rotationTask != null) rotationTask.cancel();

        if (refreshTask != null) refreshTask.cancel();
        for (UUID uuid : playerScoreboards.keySet()) {
            org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(uuid);
            if (bukkitPlayer != null) removePlayer(bukkitPlayer);
            else {
                Sidebar sidebar = playerScoreboards.get(uuid);
                if (sidebar != null) {
                    sidebar.close();
                }
            }
        }
        playerScoreboards.clear();
        scoreboardLibrary.close();
        if (instance != null) {
            instance = null;
        }
    }

    public static ScoreboardManager getInstance() {
        if (instance == null) {
            instance = new ScoreboardManager();
        }
        return instance;
    }

}
