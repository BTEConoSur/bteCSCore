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
        startRotation(config.getInt("scoreboard-rotation"), config.getInt("proyecto-scoreboard-refresh"));
    }

    /**
     * Agrega un jugador al sistema de scoreboards.
     * Crea una barra lateral para el jugador y renderiza el scoreboard actual.
     *
     * @param player jugador a agregar.
     */
    public void addPlayer(Player player) {
        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null) return;
        Sidebar sidebar = scoreboardLibrary.createSidebar();
        sidebar.addPlayer(player.getBukkitPlayer());
        playerScoreboards.put(player.getUuid(), sidebar);
        if (currentScoreboard == null) return;
        render(player, currentScoreboard);
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
        if (sidebar != null) {
            sidebar.removePlayer(player.getBukkitPlayer());
            sidebar.close();
        }
    }

    /**
     * Inicia la rotación automática entre scoreboards y configura el intervalo de refresco.
     *
     * @param seconds intervalo en segundos para rotar entre scoreboards.
     * @param proyectoRefreshSeconds intervalo en segundos para actualizar scoreboard de proyecto.
     */
    public void startRotation(int seconds, int proyectoRefreshSeconds) {
        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            globalIndex = (globalIndex + 1) % scoreboards.size();
            currentScoreboard = scoreboards.get(globalIndex);
            renderAll();
            restartRefreshTask();
        }, 0L, seconds * 20L);
    }

    /**
     * Reinicia la tarea de actualización periódica para el scoreboard actual.
     * Se invoca al cambiar de scoreboard en la rotación.
     */
    private void restartRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        if (!currentScoreboard.isRefreshable()) return;
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentScoreboard.isGlobal()) currentScoreboard.update();
            for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
                if (!player.getConfiguration().getGeneralScoreboard()) continue;
                render(player, currentScoreboard);
            }

        }, 0L, currentScoreboard.getRefreshIntervalTicks());
    }

    /**
     * Renderiza el scoreboard actual a todos los jugadores en línea que lo tengan habilitado.
     */
    private void renderAll() {
        if (currentScoreboard == null) return;

        if (currentScoreboard.isGlobal()) {
            currentScoreboard.update();
        }

        for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (!player.getConfiguration().getGeneralScoreboard()) continue;
            render(player, currentScoreboard);
        }
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

        if (refreshTask != null) refreshTask.cancel();
        for (Sidebar sidebar : playerScoreboards.values()) {
            if (sidebar != null) {
                sidebar.close();
            }
        }
        playerScoreboards.clear();
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
