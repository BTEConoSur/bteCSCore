package com.bteconosur.world.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.locationtech.jts.geom.Polygon;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.ChunkKey;
import com.bteconosur.world.WorldManager;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

/**
 * Representa el mundo principal de BTE con sistema de capas (alta y baja).
 * Gestiona el movimiento entre capas, títulos de ubicación y partículas de borde.
 */
public class BTEWorld {

    private final CapaAlta capaAlta;
    private final CapaBaja capaBaja;
    private HashMap<UUID, BukkitTask> playerTasks = new HashMap<>();
    private HashMap<UUID, Proyecto> lastProject = new HashMap<>();
    private HashMap<UUID, Division> lastDivision = new HashMap<>();
    private HashMap<UUID, ChunkKey> playerChunks = new HashMap<>();
    private boolean isValid = false;

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    private final MultiverseCoreApi multiverseApi = BTEConoSur.getMultiverseCoreApi();
  
    /**
     * Constructor que inicializa el mundo BTE con sus capas y configuraciones.
     * Carga las capas, valida el mundo y opcionalmente habilita partículas de borde.
     */
    public BTEWorld() {

        ConsoleLogger.info(LanguageHandler.getText("bte-world-loading"));
        capaBaja = new CapaBaja(config.getString("layers.capa_baja.name"), config.getString("layers.capa_baja.display-name"), config.getInt("layers.capa_baja.offset"));
        capaAlta = new CapaAlta(config.getString("layers.capa_alta.name"), config.getString("layers.capa_alta.display-name"), config.getInt("layers.capa_alta.offset"));

        loadWorld();
        if (!isValid) ConsoleLogger.error(LanguageHandler.getText("invalid-bte-world"));
        if (config.getBoolean("border-particles.label-enable")) enableParticlesSpawning();
    }

    /**
     * Carga y valida los mundos de las capas.
     * Verifica que los mundos existan y contengan regiones válidas.
     */
    private void loadWorld() {
        boolean ok = true;
        for (LabelWorld lw : List.of(capaAlta, capaBaja)) {
            if (lw.getBukkitWorld() == null) {
                ok = false;
                break;
            }
            if (lw.getRegions().isEmpty() || lw.getRegions() == null) {
                ok = false;
                ConsoleLogger.error(LanguageHandler.getText("capas-no-region").replace("%name%", lw.getDisplayName()));
                break;
            }
        }
        this.isValid = ok;
    }

    /**
     * Obtiene un LabelWorld por el nombre del mundo de Bukkit.
     * 
     * @param bukkitWorldName Nombre del mundo de Bukkit
     * @return LabelWorld correspondiente, o null si no coincide
     */
    public LabelWorld getLabelWorld(String bukkitWorldName) {
        if (capaAlta.getBukkitWorld().getName().equals(bukkitWorldName)) return capaAlta;
        if (capaBaja.getBukkitWorld().getName().equals(bukkitWorldName)) return capaBaja;
        return null;
    }

    /**
     * Determina qué LabelWorld contiene las coordenadas especificadas.
     * Verifica primero la capa baja y luego la capa alta.
     * 
     * @param x Coordenada X
     * @param z Coordenada Z
     * @return LabelWorld que contiene las coordenadas, o null si ninguna las contiene
     */
    public LabelWorld getLabelWorld(double x, double z) {
        List<RegionData> regionsb = capaBaja.getRegions();
        for (RegionData regionData : regionsb) {
            if (RegionUtils.containsCoordinate(regionData.getPrepared(), regionData.getEnvelope(), x, z)) return capaBaja;
        }
        List<RegionData> regions = capaAlta.getRegions();
        for (RegionData regionData : regions) {
            if (RegionUtils.containsCoordinate(regionData.getPrepared(), regionData.getEnvelope(), x, z)) return capaAlta;
        }
        return null;
    }

    /**
     * Verifica si una ubicación corresponde al mundo lobby.
     * 
     * @param loc Ubicación a verificar
     * @return true si la ubicación es el lobby, false en caso contrario
     */
    public boolean isLobbyLocation(Location loc) {
        if (loc == null) return false;
        return loc.getWorld().getName().equals(config.getString("lobby.world"));
    }

    /**
     * Verifica si el jugador entró a un nuevo proyecto y muestra el título correspondiente.
     * Solo muestra el título si la configuración del jugador lo permite.
     * 
     * @param toLocation Ubicación de destino
     * @param player Jugador que se mueve
     */
    public void checkProyectoMove(Location toLocation, Player player) {
        com.bteconosur.db.model.Player btecsPlayer = com.bteconosur.db.model.Player.getBTECSPlayer(player);
        if (!btecsPlayer.getConfiguration().getGeneralProjectTitle()) return;
        Set<Proyecto> proyectoTo = ProyectoRegistry.getInstance().getByLocation(toLocation.getBlockX(), toLocation.getBlockZ());
        if (proyectoTo.size() > 1) return;
        Proyecto destination = proyectoTo.stream().findFirst().orElse(null);
        if (destination == null) {
            lastProject.put(player.getUniqueId(), null);
            return;
        }
        Boolean hasPlayer = lastProject.containsKey(player.getUniqueId()); 
        Proyecto last = lastProject.get(player.getUniqueId());
        lastProject.put(player.getUniqueId(), destination);
        if (!hasPlayer) return;
        if (last != null && last == destination) return;
        Language language = btecsPlayer.getLanguage();
        String titleText = LanguageHandler.replaceMC("project-title", language, destination);
        String subtitleText = LanguageHandler.replaceMC("project-subtitle", language, destination);
        Audience audience = player;
        audience.showTitle(
            Title.title(MiniMessage.miniMessage().deserialize(titleText), 
            MiniMessage.miniMessage().deserialize(subtitleText),
            Times.times(Duration.ofMillis(config.getInt("titles-duration.project.fade-in")), Duration.ofMillis(config.getInt("titles-duration.project.stay")), Duration.ofMillis(config.getInt("titles-duration.project.fade-out"))))
        ); 
    }

    /**
     * Verifica si el jugador entró a una nueva división y muestra el título correspondiente.
     * Solo verifica cambios cuando el jugador se mueve a un chunk diferente.
     * 
     * @param toLocation Ubicación de destino
     * @param player Jugador que se mueve
     */
    public void checkDivisionMove(Location toLocation, Player player) {
        com.bteconosur.db.model.Player btecsPlayer = com.bteconosur.db.model.Player.getBTECSPlayer(player);
        if (!btecsPlayer.getConfiguration().getGeneralDivisionTitle()) return;
        
        UUID pUuid = player.getUniqueId();
        ChunkKey currentChunk = ChunkKey.fromBlock(toLocation.getBlockX(), toLocation.getBlockZ());
        ChunkKey lastChunk = playerChunks.get(pUuid);
        
        if (lastChunk != null && lastChunk.equals(currentChunk)) return;
        
        Pais paisTo = PaisRegistry.getInstance().findByLocation(toLocation.getX(), toLocation.getZ());
        Division divisionTo = PaisRegistry.getInstance().findDivisionByLocation(toLocation.getX(), toLocation.getZ(), paisTo);
        playerChunks.put(pUuid, currentChunk);
        
        if (divisionTo == null) { 
            lastDivision.remove(pUuid);
            return;
        }
        Division last = lastDivision.get(pUuid);
        if (last != null && last == divisionTo) return;
        lastDivision.put(pUuid, divisionTo);
        Language language = btecsPlayer.getLanguage();
        String titleText = LanguageHandler.replaceMC("division-title", language, divisionTo);
        String subtitleText = LanguageHandler.replaceMC("division-subtitle", language, divisionTo);
        Audience audience = player;
        audience.showTitle(
            Title.title(MiniMessage.miniMessage().deserialize(titleText), 
            MiniMessage.miniMessage().deserialize(subtitleText),
            Times.times(Duration.ofMillis(config.getInt("titles-duration.division.fade-in")), Duration.ofMillis(config.getInt("titles-duration.division.stay")), Duration.ofMillis(config.getInt("titles-duration.division.fade-out"))))
        ); 
    }

    /**
     * Verifica si el jugador cambió de capa y gestiona el teletransporte automático.
     * Inicia un contador de tiempo antes de teletransportar al jugador a la capa correcta.
     * 
     * @param lTo Ubicación de destino
     * @param player Jugador que se mueve
     */
    public void checkLayerMove(Location lTo, Player player) {
        LabelWorld currentlw = getLabelWorld(lTo.getX(), lTo.getZ());
        UUID pUuid = player.getUniqueId();
        Language language = com.bteconosur.db.model.Player.getBTECSPlayer(player).getLanguage();
        if (isLobbyLocation(lTo)) return;
        if (currentlw == null) return;
        if (currentlw.isValidLocation(lTo)) return;

        int tpCooldownSeconds = config.getInt("tp-cooldown-seconds");
        if (!currentlw.isValidLocation(lTo) && !playerTasks.containsKey(pUuid)) {
            BukkitTask task = new BukkitRunnable() {
                Integer elapsedSeconds = 0;
                @Override
                public void run() {
                    Location playerLocation = player.getLocation();
                    if (isLobbyLocation(playerLocation)) {
                        playerTasks.remove(pUuid);
                        this.cancel();
                        return;
                    }
                    LabelWorld destination = getLabelWorld(player.getX(), player.getZ());
                    if (destination == null) {
                        playerTasks.remove(pUuid);
                        this.cancel();
                        return;
                    }
                    
                    if (destination.isValidLocation(playerLocation)) {
                        this.cancel();     
                        playerTasks.remove(pUuid);
                        return;
                    };
                    
                    if (elapsedSeconds >= tpCooldownSeconds) {
                        this.cancel();
                        LabelWorld lastDestination = getLabelWorld(playerLocation.getWorld().getName());
                        ConsoleLogger.debug("Teleporting player " + player.getName() + " to layer " + destination.getDisplayName() + " from layer " + lastDestination.getDisplayName());
                        ConsoleLogger.debug("Player location before teleport: " + player.getLocation().toString());
                        ConsoleLogger.debug("Player Y before conversion: " + convertY(player.getY(), lastDestination, destination));
                        destination.teleportPlayer(player, player.getX(), convertY(player.getY(), lastDestination, destination), player.getZ(), player.getYaw(), player.getPitch());
                        playerTasks.remove(pUuid);
                        return;
                    }
                    String titleText = LanguageHandler.getText(language, "teleport-layer-title").replace("%destination%", destination.getDisplayName());
                    String subtitleText = LanguageHandler.getText(language, "teleport-layer-subtitle").replace("%seconds%", String.valueOf(tpCooldownSeconds - elapsedSeconds));
                    Audience audience = player;
                    audience.showTitle(
                        Title.title(MiniMessage.miniMessage().deserialize(titleText), 
                        MiniMessage.miniMessage().deserialize(subtitleText),
                        Times.times(Duration.ofMillis(config.getInt("titles-duration.layer-teleport.fade-in")), Duration.ofMillis(config.getInt("titles-duration.layer-teleport.stay")), Duration.ofMillis(config.getInt("titles-duration.layer-teleport.fade-out"))))
                    );
                    elapsedSeconds += 1;
                }
            }.runTaskTimer(BTEConoSur.getInstance(), 0L, 20L);
            playerTasks.put(pUuid, task);
            return;
        }
    }

    /**
     * Verifica si el jugador puede moverse entre dos ubicaciones sin salir de los límites de los países.
     * Teletransporta al jugador al lobby si se encuentra en una ubicación inválida.
     * 
     * @param fromLocation Ubicación de origen
     * @param toLocation Ubicación de destino
     * @param player Jugador que se mueve
     * @return true si el movimiento es válido, false si debe cancelarse
     */
    public boolean checkPaisMove(Location fromLocation, Location toLocation, Player player) {
        Pais paisFrom = PaisRegistry.getInstance().findByLocation(fromLocation.getX(), fromLocation.getZ());
        if (paisFrom == null) {
            PlayerLogger.warn(com.bteconosur.db.model.Player.getBTECSPlayer(player), LanguageHandler.getText(com.bteconosur.db.model.Player.getBTECSPlayer(player).getLanguage(), "not-limbo"), (String) null);
            player.teleport(multiverseApi.getWorldManager().getLoadedWorld(config.getString("lobby.world")).get().getSpawnLocation());
            return true;
        };
        Pais paisTo = PaisRegistry.getInstance().findByLocation(toLocation.getX(), toLocation.getZ());
        if (paisTo == null) return false;
        return true;
    }

    /**
     * Verifica si unas coordenadas están dentro de un país válido.
     * 
     * @param x Coordenada X
     * @param z Coordenada Z
     * @return true si las coordenadas están en un país, false en caso contrario
     */
    public boolean checkPaisMove(double x, double z) {
        Pais paisTo = PaisRegistry.getInstance().findByLocation(x, z);
        if (paisTo == null) return false;
        return true;
    }

    /**
     * Convierte la coordenada Y de una capa a otra considerando sus offsets.
     * 
     * @param ySource Coordenada Y original
     * @param sourceLw Capa de origen
     * @param destLw Capa de destino
     * @return Coordenada Y ajustada para la capa de destino
     */
    private double convertY(double ySource, LabelWorld sourceLw, LabelWorld destLw) {
        return ySource - destLw.getOffset() + sourceLw.getOffset();
    }

    /**
     * Indica si el mundo BTE se cargó correctamente.
     * 
     * @return true si el mundo es válido, false en caso contrario
     */
    public boolean isValid() {
        return this.isValid;
    }
    
    /**
     * Limpia todas las tareas y datos de tracking de un jugador.
     * 
     * @param pUuid UUID del jugador
     */
    public void clearPlayerTasks(UUID pUuid) {
        if (playerTasks.containsKey(pUuid)) {
            playerTasks.get(pUuid).cancel();
            playerTasks.remove(pUuid);
        }
        playerChunks.remove(pUuid);
    }

    /**
     * Apaga el sistema del mundo, cancelando todas las tareas programadas.
     */
    public void shutdown() {
        playerTasks.values().forEach(task -> {
            if (!task.isCancelled()) task.cancel();
        });
        playerTasks.clear();
    }

    /**
     * Obtiene la API de Multiverse-Core.
     * 
     * @return Instancia de MultiverseCoreApi
     */
    public MultiverseCoreApi getMultiverseApi() {
        return this.multiverseApi;
    }

    /**
     * Habilita el spawn periódico de partículas en los bordes de las capas para jugadores.
     * Solo muestra partículas si el jugador tiene la configuración habilitada.
     */
    private void enableParticlesSpawning() {
        long periodTicks = ConfigHandler.getInstance().getConfig().getLong("border-particles.spawn-period");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (com.bteconosur.db.model.Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
                    if (player == null) continue;
                    if (!player.getConfiguration().getGeneralLabelBorder()) continue;
                    Player bukkitPlayer = Bukkit.getPlayer(player.getUuid());
                    if (bukkitPlayer == null) continue;
                    if (WorldManager.getInstance().getBTEWorld().isLobbyLocation(bukkitPlayer.getLocation())) continue;
                    
                    Location location = bukkitPlayer.getLocation();
                    LabelWorld currentLabelWorld = WorldManager.getInstance().getBTEWorld().getLabelWorld(location.getX(), location.getZ());
                    if (currentLabelWorld == null) continue;
                    Polygon polygon = currentLabelWorld.getPolygonForPlayer(player);
                    if (polygon == null) continue;
                    RegionUtils.spawnBorderParticles(bukkitPlayer, polygon, config.getString("border-particles.label-particle"));
                }
            }
        }.runTaskTimer(BTEConoSur.getInstance(), 0L, periodTicks);
    }
}
