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

/**
 * Gestor centralizado de scoreboards del servidor.
 * Administra la rotación automática entre diferentes tipos de scoreboards, su actualización
 * de datos periódica y asignación a jugadores individuales cuando corresponda.
 * Implementa patrón singleton.
 */
public class ScoreboardManager {
    
    private static ScoreboardManager instance;
    private ScoreboardLibrary scoreboardLibrary;
    private final BTEConoSur plugin;

    private Map<UUID, Sidebar> playerScoreboards = new HashMap<>();
    private Map<UUID, Integer> playerIndices = new HashMap<>();
    private List<Scoreboard> scoreboards = List.of(
        new OnlineScoreboard(),
        new PlayerScoreboard(),
        new ProyectoScoreboard()
    );

    private BukkitTask rotationTask;
    private Map<Scoreboard, BukkitTask> refreshTasks = new HashMap<>();

    private final YamlConfiguration config;
    
    /**
     * Crea e inicializa el gestor de scoreboards, cargando la biblioteca de scoreboards
     * e iniciando la rotación automática según la configuración.
     */
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
        startRotation(config.getInt("scoreboard-rotation"));
        startRefreshTasks();
    }

    /**
     * Agrega un jugador al sistema de scoreboards.
     * Crea una barra lateral para el jugador y renderiza el primer scoreboard habilitado.
     *
     * @param player jugador a agregar.
     */
    public void addPlayer(Player player) {
        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null) return;
        Sidebar sidebar = scoreboardLibrary.createSidebar();
        sidebar.addPlayer(player.getBukkitPlayer());
        playerScoreboards.put(player.getUuid(), sidebar);
        Scoreboard next = getNextEnabledScoreboard(player);
        if (next == null) return;
        render(player, next);
    }

    /**
     * Elimina un jugador del sistema de scoreboards.
     * Cierra su barra lateral y libera sus recursos.
     *
     * @param player jugador a eliminar.
     */
    public void removePlayer(Player player) {
        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null) return;
        Sidebar sidebar = playerScoreboards.remove(player.getUuid());
        playerIndices.remove(player.getUuid());
        if (sidebar != null) {
            sidebar.removePlayer(player.getBukkitPlayer());
            sidebar.close();
        }
    }

    /**
     * Inicia la rotación automática por jugador entre scoreboards habilitados.
     *
     * @param seconds intervalo en segundos para rotar entre scoreboards.
     */
    public void startRotation(int seconds) {
        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
                if (!player.getConfiguration().getGeneralScoreboard()) continue;
                Scoreboard next = getNextEnabledScoreboard(player);
                if (next == null) continue;
                if (next.isGlobal()) next.update();
                render(player, next);
            }
        }, 0L, seconds * 20L);
    }

    /**
     * Inicia tareas de refresco independientes para cada scoreboard que lo requiera.
     * Cada tarea corre a su propio intervalo y solo renderiza a jugadores que estén viendo ese scoreboard.
     */
    private void startRefreshTasks() {
        for (Scoreboard sb : scoreboards) {
            if (!sb.isRefreshable()) continue;
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (sb.isGlobal()) sb.update();
                for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
                    if (!player.getConfiguration().getGeneralScoreboard()) continue;
                    int index = playerIndices.getOrDefault(player.getUuid(), 0);
                    if (scoreboards.get(index) != sb) continue;
                    render(player, sb);
                }
            }, 0L, sb.getRefreshIntervalTicks());
            refreshTasks.put(sb, task);
        }
    }

    /**
     * Detiene todas las tareas de refresco de scoreboards.
     */
    private void stopRefreshTasks() {
        for (BukkitTask task : refreshTasks.values()) {
            task.cancel();
        }
        refreshTasks.clear();
    }

    /**
     * Obtiene el siguiente scoreboard habilitado para un jugador, saltando los desactivados.
     *
     * @param player jugador para el que se busca el siguiente scoreboard.
     * @return el siguiente scoreboard habilitado, o {@code null} si todos están deshabilitados.
     */
    private Scoreboard getNextEnabledScoreboard(Player player) {
        int current = playerIndices.getOrDefault(player.getUuid(), -1);
        for (int i = 1; i <= scoreboards.size(); i++) {
            int index = (current + i) % scoreboards.size();
            Scoreboard sb = scoreboards.get(index);
            if (sb.isEnabledFor(player.getConfiguration())) {
                playerIndices.put(player.getUuid(), index);
                return sb;
            }
        }
        return null;
    }

    /**
     * Renderiza un scoreboard específico a un jugador individual.
     *
     * @param player jugador para renderizar.
     * @param scoreboard scoreboard a renderizar.
     */
    private void render(Player player, Scoreboard scoreboard) {
        Sidebar sidebar = playerScoreboards.get(player.getUuid());
        if (sidebar == null) return;
        ComponentSidebarLayout layout = scoreboard.getLayout(player, player.getLanguage());
        layout.apply(sidebar);
    }

    /**
     * Detiene el gestor de scoreboards y libera todos los recursos.
     * Cancela tareas de rotación/refresco, elimina scoreboards de jugadores y cierra la biblioteca.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("scoreboard-manager-shutting-down"));
        if (rotationTask != null) rotationTask.cancel();
        stopRefreshTasks();
        for (Sidebar sidebar : playerScoreboards.values()) {
            if (sidebar != null) {
                sidebar.close();
            }
        }
        playerScoreboards.clear();
        playerIndices.clear();
        scoreboardLibrary.close();
        if (instance != null) {
            instance = null;
        }
    }

    /**
     * Obtiene la instancia única del gestor de scoreboards (patrón singleton).
     *
     * @return instancia única del gestor.
     */
    public static ScoreboardManager getInstance() {
        if (instance == null) {
            instance = new ScoreboardManager();
        }
        return instance;
    }

}
