package com.bteconosur.db.registry;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;

public abstract class Registry<K extends Serializable, V> {

    protected ConcurrentHashMap<K, V> loadedObjects = new ConcurrentHashMap<>();

    protected final DBManager dbManager;
    protected final YamlConfiguration config;
    
    public Registry() {
        dbManager = DBManager.getInstance();
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig(); 
    }

    public Map<K, V> getMap() {
        if (loadedObjects == null) return Collections.emptyMap();
        return loadedObjects;
    }

    public List<V> getList() {
        if (loadedObjects == null) return Collections.emptyList();
        return List.copyOf(loadedObjects.values());
    }

    public Set<K> getIds() {
        if (loadedObjects == null) return Collections.emptySet();
        return new HashSet<>(loadedObjects.keySet());
    }

    public boolean isLoaded(K id) {
        if (loadedObjects == null) return false;
        return loadedObjects.containsKey(id);
    }

    public boolean isObjLoaded(V obj) {
        if (loadedObjects == null) return false;
        return loadedObjects.containsValue(obj);
    }
    
    public abstract void load(V obj);

    @SuppressWarnings("unchecked")
    public V merge(K id) {
        if (id == null || loadedObjects == null) return null;
        V obj = loadedObjects.get(id);
        if (obj == null) return null;
        V mergedObj = (V) dbManager.merge(obj);
        if (mergedObj != null) loadedObjects.put(id, mergedObj);
        return mergedObj;
    }

    public void unload(K id) {
        if (id == null || loadedObjects == null) return;
        loadedObjects.remove(id);
    }

    public boolean exists(K id) {
        if (id == null || loadedObjects == null) return false;
        return loadedObjects.containsKey(id);
    }

    public V get(K id) {
        if (id == null || loadedObjects == null) return null;
        return loadedObjects.get(id);
    }

    public abstract void shutdown();

}
