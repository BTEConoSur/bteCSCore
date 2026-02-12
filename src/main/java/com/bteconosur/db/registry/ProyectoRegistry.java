package com.bteconosur.db.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.ChunkKey;
import com.bteconosur.db.util.Estado;

public class ProyectoRegistry extends Registry<String, Proyecto> {

    private static ProyectoRegistry instance;

    private ConcurrentHashMap<ChunkKey, List<String>> loadedChunkProyectos = new ConcurrentHashMap<>();

    public ProyectoRegistry() {
        super();
        ConsoleLogger.info(lang.getString("proyecto-registry-initializing"));  
        loadedObjects = new ConcurrentHashMap<>();
        List<Proyecto> proyectos = dbManager.selectAll(Proyecto.class);
        if (proyectos != null) {
            for (Proyecto p : proyectos) {
                loadedObjects.put(p.getId(), p);
                Set<ChunkKey> chunkKeys = RegionUtils.chunksFor(p);
                for (ChunkKey chunkKey : chunkKeys) loadedChunkProyectos.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(p.getId());
            }
        }
    }

    @Override
    public void load(Proyecto obj) {
        if (obj == null || obj.getId() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
        Set<ChunkKey> chunkKeys = RegionUtils.chunksFor(obj);
        for (ChunkKey chunkKey : chunkKeys) loadedChunkProyectos.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(obj.getId());
    }
    
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

    @Override
    public Proyecto get(String id) {
        if (id == null) return null;
        return loadedObjects.get(id.toUpperCase());
    }

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

    public Set<Proyecto> getByPlayer(Player player) {
        Set<Proyecto> proyectos = new HashSet<>();
        PermissionManager pm = PermissionManager.getInstance();
        for (Proyecto proyecto : loadedObjects.values()) {
            if (pm.isLider(player, proyecto) || pm.isMiembro(player, proyecto)) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    public Set<Proyecto> getByLider(Player player, Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (PermissionManager.getInstance().isLider(player, proyecto)) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

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

    public Set<Proyecto> getActive(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.ACTIVO) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    public Set<Proyecto> getActiveOrEditando(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.ACTIVO || proyecto.getEstado() == Estado.EDITANDO) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    public Set<Proyecto> getFinishing(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.EN_FINALIZACION || proyecto.getEstado() == Estado.EN_FINALIZACION_EDICION) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

    public Set<Proyecto> getCompleted(Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            if (proyecto.getEstado() == Estado.COMPLETADO) {
                proyectos.add(proyecto);
            }
        }
        return proyectos;
    }

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

    public boolean hasCollisions(String proyectoId, Polygon poligono) {
        Set<Proyecto> overlapping = getOverlapping(proyectoId, poligono);
        return !overlapping.isEmpty();
    }

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

    public Set<Proyecto> getByLocation(int x, int z, Set<Proyecto> search) {
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            Polygon poly = proyecto.getPoligono();
            if (poly != null && RegionUtils.containsCoordinate(poly, x, z)) proyectos.add(proyecto);
        }
        return proyectos;
    }

    public Set<Proyecto> getByLocation(int x, int z) {
        ChunkKey chunkKey = ChunkKey.fromBlock(x, z);
        Set<Proyecto> search = getByChunk(chunkKey);
        Set<Proyecto> proyectos = new HashSet<>();
        for (Proyecto proyecto : search) {
            Polygon poly = proyecto.getPoligono();
            if (poly != null && RegionUtils.containsCoordinate(poly, x, z)) proyectos.add(proyecto);
        }
        return proyectos;
    }

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

    public void shutdown() {
        ConsoleLogger.info(lang.getString("proyecto-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
        loadedChunkProyectos.clear();
        loadedChunkProyectos = null;
    }

    public static ProyectoRegistry getInstance() {
        if (instance == null) {
            instance = new ProyectoRegistry();
        }
        return instance;
    }

}
