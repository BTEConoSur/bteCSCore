package com.bteconosur.db.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.ChunkKey;
import com.bteconosur.db.util.Estado;
import com.bteconosur.world.WorldManager;

/**
 * Registro de proyectos con índices auxiliares por chunk y partículas de borde.
 */
public class ProyectoRegistry extends Registry<String, Proyecto> {

    private static ProyectoRegistry instance;

    private ConcurrentHashMap<ChunkKey, List<String>> loadedChunkProyectos = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, String> playerBorderParticles = new ConcurrentHashMap<>();

    /**
     * Inicializa el registro, carga proyectos y construye índices por chunk.
     */
    public ProyectoRegistry() {
        super();
        ConsoleLogger.info(LanguageHandler.getText("proyecto-registry-initializing"));  
        loadedObjects = new ConcurrentHashMap<>();
        List<Proyecto> proyectos = dbManager.selectAll(Proyecto.class);
        if (proyectos != null) {
            for (Proyecto p : proyectos) {
                loadedObjects.put(p.getId(), p);
                Set<ChunkKey> chunkKeys = RegionUtils.chunksFor(p);
                for (ChunkKey chunkKey : chunkKeys) loadedChunkProyectos.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(p.getId());
            }
        }
        if (config.getBoolean("border-particles.project-enable")) enableParticlesSpawning();
    }

    /**
     * Carga un proyecto en persistencia y memoria, actualizando el índice por chunk.
     *
     * @param obj proyecto a cargar.
     */
    @Override
    public void load(Proyecto obj) {
        if (obj == null || obj.getId() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
        Set<ChunkKey> chunkKeys = RegionUtils.chunksFor(obj);
        for (ChunkKey chunkKey : chunkKeys) loadedChunkProyectos.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(obj.getId());
    }
    
    /**
     * Guarda un proyecto y reconstruye su indexación por chunk.
     *
     * @param id id del proyecto.
     * @return proyecto guardado, o {@code null}.
     */
    @Override
    public Proyecto merge(String id) {
        if (id == null) return null;
        Proyecto obj = loadedObjects.get(id.toUpperCase());
        if (obj == null) return null;
        removeFromChunkIndex(obj);
        Proyecto mergedObj = (Proyecto) dbManager.merge(obj);
        if (mergedObj != null) {
            Set<ChunkKey> chunkKeys = RegionUtils.chunksFor(mergedObj);
            for (ChunkKey chunkKey : chunkKeys) loadedChunkProyectos.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(mergedObj.getId());
            loadedObjects.put(id.toUpperCase(), mergedObj);
        }
        return mergedObj;
    }

    /**
     * Obtiene un proyecto por id normalizado en mayúsculas.
     *
     * @param id id del proyecto.
     * @return proyecto encontrado, o {@code null}.
     */
    @Override
    public Proyecto get(String id) {
        if (id == null) return null;
        return loadedObjects.get(id.toUpperCase());
    }

    /**
     * Descarga un proyecto y lo remueve de persistencia e índice espacial.
     *
     * @param id id del proyecto.
     */
    @Override
    public void unload(String id) {
        if (id == null) return;
        Proyecto proyecto = loadedObjects.get(id.toUpperCase());
        if (proyecto != null) {
            removeFromChunkIndex(proyecto);
        }
        loadedObjects.remove(id.toUpperCase());
        dbManager.remove(proyecto);
    }

    /**
     * Obtiene proyectos de un jugador priorizando liderados y luego miembros.
     *
     * @param player jugador objetivo.
     * @return conjunto ordenado de proyectos asociados.
     */
    public LinkedHashSet<Proyecto> getByPlayer(Player player) {
        LinkedHashSet<Proyecto> proyectos = new LinkedHashSet<>();
        Set<Proyecto> proyectosMiembro = new HashSet<>();
        Set<Proyecto> proyectosLider = new HashSet<>();
        PermissionManager pm = PermissionManager.getInstance();
        for (Proyecto proyecto : loadedObjects.values()) {
            if (pm.isLider(player, proyecto)) {
                proyectosLider.add(proyecto);
            } else if (pm.isMiembro(player, proyecto)) {
                proyectosMiembro.add(proyecto);
            }
        }
        proyectos.addAll(proyectosLider);
        proyectos.addAll(proyectosMiembro);
        return proyectos;
    }

    /**
     * Obtiene IDs de proyectos donde el jugador es líder.
     *
     * @param player jugador objetivo.
     * @return conjunto de ids de proyectos liderados.
     */
    public Set<String> getIdsByLider(Player player) {
        Set<String> proyectos = new HashSet<>();
        PermissionManager pm = PermissionManager.getInstance();
        for (Proyecto proyecto : loadedObjects.values()) {
            if (pm.isLider(player, proyecto)) {
                proyectos.add(proyecto.getId());
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos donde el jugador es líder.
     *
     * @param player jugador objetivo.
     * @param search conjunto de búsqueda.
     * @return subconjunto donde el jugador es líder.
     */
    public Set<Proyecto> getByLider(Player player, Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (PermissionManager.getInstance().isLider(player, proyecto)) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos donde el jugador no es miembro ni líder.
     *
     * @param player jugador objetivo.
     * @param search conjunto de búsqueda.
     * @return subconjunto sin relación de membresía/liderazgo.
     */
    public Set<Proyecto> getNotMemberOrLider(Player player, Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        PermissionManager pm = PermissionManager.getInstance();
        for (Proyecto proyecto : search) {
            if (!pm.isMiembroOrLider(player, proyecto)) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos donde el jugador es miembro o líder.
     *
     * @param player jugador objetivo.
     * @param search conjunto de búsqueda.
     * @return subconjunto con relación de membresía/liderazgo.
     */
    public Set<Proyecto> getMemberOrLider(Player player, Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        PermissionManager pm = PermissionManager.getInstance();
        for (Proyecto proyecto : search) {
            if (pm.isMiembroOrLider(player, proyecto)) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos que tienen cupo para nuevos miembros.
     *
     * @param search conjunto de búsqueda.
     * @return subconjunto de proyectos con lugar disponible.
     */
    public Set<Proyecto> getWithRoom(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            PermissionManager pm = PermissionManager.getInstance();
            Player lider = proyecto.getLider();
            if (lider == null) continue;
            if (pm.isPostulante(lider) && proyecto.getCantMiembros() >= config.getInt("max-members-for-postulantes")) continue;
            if (proyecto.checkMaxMiembros()) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos en estado activo.
     *
     * @param search conjunto de búsqueda.
     * @return subconjunto activo.
     */
    public Set<Proyecto> getActive(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.ACTIVO) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos en estado abandonado.
     *
     * @param search conjunto de búsqueda.
     * @return subconjunto abandonado.
     */
    public Set<Proyecto> getAbandoned(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.ABANDONADO) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos activos o en edición.
     *
     * @param search conjunto de búsqueda.
     * @return subconjunto activo o editando.
     */
    public Set<Proyecto> getActiveOrEditando(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.ACTIVO || proyecto.getEstado() == Estado.EDITANDO) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos en finalización o finalización de edición.
     *
     * @param search conjunto de búsqueda.
     * @return subconjunto en estado de finalización.
     */
    public Set<Proyecto> getFinishing(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.EN_FINALIZACION || proyecto.getEstado() == Estado.EN_FINALIZACION_EDICION) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos en estado completado.
     *
     * @param search conjunto de búsqueda.
     * @return subconjunto completado.
     */
    public Set<Proyecto> getCompleted(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.COMPLETADO) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Obtiene conteo de proyectos liderados por estado.
     *
     * @param player jugador líder.
     * @return arreglo con [completados, activos].
     */
    public int[] getCounts(Player player) { // returns [Finalizados, Activos]
        int[] count = new int[2];
        PermissionManager pm = PermissionManager.getInstance();
        for (Proyecto proyecto : loadedObjects.values()) {
            if (pm.isLider(player, proyecto)) {
                if(proyecto.getEstado() == Estado.COMPLETADO) count[0]++;
                if(proyecto.getEstado() == Estado.ACTIVO) count[1]++;
            }
        }
        return count;
    }

    /**
     * Cuenta proyectos completados liderados por un jugador.
     *
     * @param player jugador líder.
     * @return cantidad de proyectos completados.
     */
    public int getCompletadosCount(Player player) {
        int count = 0;
        PermissionManager pm = PermissionManager.getInstance();
        for (Proyecto proyecto : loadedObjects.values()) {
            if (pm.isLider(player, proyecto)) {
                if(proyecto.getEstado() == Estado.COMPLETADO) count++;
            }
        }
        return count;
    }

    /**
     * Cuenta proyectos activos liderados por un jugador.
     *
     * @param player jugador líder.
     * @return cantidad de proyectos activos.
     */
    public int getActivosCount(Player player) {
        int count = 0;
        PermissionManager pm = PermissionManager.getInstance();
        for (Proyecto proyecto : loadedObjects.values()) {
            if (pm.isLider(player, proyecto)) {
                if(proyecto.getEstado() == Estado.ACTIVO) count++;
            }
        }
        return count;
    }

    /**
     * Obtiene proyectos que se superponen con un polígono dado.
     *
     * @param proyectoId id del proyecto de referencia.
     * @param poligono polígono a comparar.
     * @return conjunto de proyectos superpuestos.
     */
    public Set<Proyecto> getOverlapping(String proyectoId, Polygon poligono) {
        Set<Proyecto> proyectos = new HashSet<>();
        Set<ChunkKey> chunkKeys = loadedChunkProyectos.entrySet().stream()
            .filter(entry -> entry.getValue().contains(proyectoId))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        for (ChunkKey chunkKey : chunkKeys) {
            List<String> proyectoIds = loadedChunkProyectos.get(chunkKey);
            if (proyectoIds == null) continue;
            for (String otherProyectoId : proyectoIds) {
                if (otherProyectoId.equals(proyectoId)) continue;
                Proyecto otherProyecto = loadedObjects.get(otherProyectoId);
                if (poligono.intersects(otherProyecto.getPoligono()))  proyectos.add(otherProyecto);
            }
        }
        return proyectos;
    }

    /**
     * Verifica si un polígono tiene colisiones con otros proyectos.
     *
     * @param proyectoId id del proyecto de referencia.
     * @param poligono polígono a evaluar.
     * @return {@code true} si hay superposiciones.
     */
    public boolean hasCollisions(String proyectoId, Polygon poligono) {
        Set<Proyecto> overlapping = getOverlapping(proyectoId, poligono);
        return !overlapping.isEmpty();
    }

    /**
     * Obtiene proyectos indexados en un chunk.
     *
     * @param chunkKey clave de chunk.
     * @return conjunto de proyectos asociados al chunk.
     */
    public Set<Proyecto> getByChunk(ChunkKey chunkKey) {
        Set<Proyecto> proyectos = new HashSet<>();
        List<String> proyectoIds = loadedChunkProyectos.get(chunkKey);
        if (proyectoIds != null) {
            for (String proyectoId : proyectoIds) {
                Proyecto proyecto = loadedObjects.get(proyectoId);
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    /**
     * Filtra proyectos de un conjunto por ubicación XZ.
     *
     * @param x coordenada X.
     * @param z coordenada Z.
     * @param search conjunto de búsqueda.
     * @return proyectos que contienen la ubicación.
     */
    public Set<Proyecto> getByLocation(int x, int z, Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            Polygon poly = proyecto.getPoligono();
            if (poly != null && RegionUtils.containsCoordinate(proyecto.getPreparedGeometry(), proyecto.getBoundingBox(), x, z)) proyectos.add(proyecto);
        }
        return proyectos;
    }

    /**
     * Busca proyectos en una ubicación XZ usando índice por chunk.
     *
     * @param x coordenada X.
     * @param z coordenada Z.
     * @return proyectos que contienen la ubicación.
     */
    public Set<Proyecto> getByLocation(int x, int z) {
        ChunkKey chunkKey = ChunkKey.fromBlock(x, z);
        Set<Proyecto> search = getByChunk(chunkKey);
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            Polygon poly = proyecto.getPoligono();
            if (poly != null && RegionUtils.containsCoordinate(proyecto.getPreparedGeometry(), proyecto.getBoundingBox(), x, z)) proyectos.add(proyecto);
        }
        return proyectos;
    }

    /**
     * Remueve un proyecto del índice de chunks según su geometría actual.
     *
     * @param proyecto proyecto a desindexar.
     */
    private void removeFromChunkIndex(Proyecto proyecto) {
        Set<ChunkKey> oldChunks = RegionUtils.chunksFor(proyecto);
        for (ChunkKey chunkKey : oldChunks) {
            List<String> ids = loadedChunkProyectos.get(chunkKey);
            if (ids != null) {
                ids.remove(proyecto.getId());
                if (ids.isEmpty()) loadedChunkProyectos.remove(chunkKey);
            }
        }
    }

    /**
     * Activa el borde de partículas de proyecto para un jugador.
     *
     * @param playerId uuid del jugador.
     * @param particleName id del proyecto a mostrar.
     */
    public void addPlayerBorderParticle(UUID playerId, String particleName) {
        playerBorderParticles.put(playerId, particleName);
    }

    /**
     * Desactiva el borde de partículas de proyecto para un jugador.
     *
     * @param playerId uuid del jugador.
     */
    public void removePlayerBorderParticle(UUID playerId) {
        playerBorderParticles.remove(playerId);
    }

    /**
     * Obtiene el proyecto cuyo borde de partículas se muestra a un jugador.
     *
     * @param playerId uuid del jugador.
     * @return id del proyecto asociado, o {@code null}.
     */
    public String getPlayerBorderParticle(UUID playerId) {
        return playerBorderParticles.get(playerId);
    }   

    /**
     * Activa el renderizado periódico de partículas de bordes de proyecto.
     */
    private void enableParticlesSpawning() {
        long periodTicks = ConfigHandler.getInstance().getConfig().getLong("border-particles.spawn-period");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                    if (!playerBorderParticles.containsKey(player.getUniqueId())) continue;
                    if (WorldManager.getInstance().getBTEWorld().isLobbyLocation(player.getLocation())) continue;
                    Proyecto proyecto = get(playerBorderParticles.get(player.getUniqueId()));
                    if (proyecto == null || proyecto.getPoligono() == null) continue;
                    RegionUtils.spawnBorderParticles(player, proyecto.getPoligono(), config.getString("border-particles.project-particle"), 0.5);
                }
            }
        }.runTaskTimer(BTEConoSur.getInstance(), 0L, periodTicks);
    }

    /**
     * Cierra el registro y limpia sus estructuras en memoria.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText( "proyecto-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
        loadedChunkProyectos.clear();
        loadedChunkProyectos = null;
    }

    /**
     * Obtiene la instancia singleton de {@code ProyectoRegistry}.
     *
     * @return instancia única del registro.
     */
    public static ProyectoRegistry getInstance() {
        if (instance == null) {
            instance = new ProyectoRegistry();
        }
        return instance;
    }

}
