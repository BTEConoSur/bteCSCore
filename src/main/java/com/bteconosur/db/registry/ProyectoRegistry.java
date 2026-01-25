package com.bteconosur.db.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.ChunkKey;

public class ProyectoRegistry extends Registry<String, Proyecto> {

    private static ProyectoRegistry instance;

    private ConcurrentHashMap<ChunkKey, List<String>> loadedChunkProyectos = new ConcurrentHashMap<>();

    public ProyectoRegistry() {
        super();
        ConsoleLogger.info(lang.getString("proyecto-registry-initializing"));  
        loadedObjects = new ConcurrentHashMap<>();
        List<Proyecto> proyectos = dbManager.selectAll(Proyecto.class);
        if (proyectos != null) for (Proyecto p : proyectos) loadedObjects.put(p.getId(), p);
    }

    @Override
    public void load(Proyecto obj) {
        if (obj == null || obj.getId() == null) return;
        loadedObjects.put(obj.getId(), obj);
        Set<ChunkKey> chunkKeys = RegionUtils.chunksFor(obj);
        for (ChunkKey chunkKey : chunkKeys) loadedChunkProyectos.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(obj.getId());
        dbManager.save(obj);
    }

    @Override
    public Proyecto merge(String id) {
        if (id == null) return null;
        Proyecto obj = loadedObjects.get(id);
        if (obj == null) return null;
        removeFromChunkIndex(obj);
        Proyecto mergedObj = (Proyecto) dbManager.merge(obj);
        if (mergedObj != null) {
            Set<ChunkKey> chunkKeys = RegionUtils.chunksFor(mergedObj);
            for (ChunkKey chunkKey : chunkKeys) loadedChunkProyectos.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(mergedObj.getId());
            loadedObjects.put(id, mergedObj);
        }
        return mergedObj;
    }

    @Override
    public void unload(String id) {
        if (id == null) return;
        Proyecto proyecto = loadedObjects.get(id);
        if (proyecto != null) {
            removeFromChunkIndex(proyecto);
        }
        loadedObjects.remove(id);
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
