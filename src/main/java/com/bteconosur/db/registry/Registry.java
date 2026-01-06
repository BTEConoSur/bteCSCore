package com.bteconosur.db.registry;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.DBManager;

public abstract class Registry<K extends Serializable, V> {

    protected ConcurrentHashMap<K, V> loadedObjects = new ConcurrentHashMap<>();

    protected final DBManager dbManager;
    protected final YamlConfiguration lang;
    protected final YamlConfiguration config;
    protected final ConsoleLogger logger;

    private final Class<V> clazz;
    
    public Registry(Class<V> clazz) {
        this.clazz = clazz;
        dbManager = DBManager.getInstance();
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        config = configHandler.getConfig(); 
        logger = BTEConoSur.getConsoleLogger();
    }


    public Map<K, V> getMap() {
        return loadedObjects;
    }

    public List<V> getList() {
        return List.copyOf(loadedObjects.values());
    }

    public Set<K> getIds() {
        return new HashSet<>(loadedObjects.keySet());
    }

    public boolean isLoaded(K id) {
        return loadedObjects.containsKey(id);
    }

    public boolean isObjLoaded(V obj) {
        return loadedObjects.containsValue(obj);
    }
    
    public abstract void load(V obj);

    public void merge(K id) {
        if (id == null) return;
        V obj = loadedObjects.get(id);
        if (obj == null) return;
        dbManager.merge(obj);
    }

    public void unload(K id) {
        if (id == null) return;
        loadedObjects.remove(id);
    }

    public boolean exists(K id) {
        if (id == null) return false;
        return loadedObjects.containsKey(id);
    }

    public V get(K id) {
        if (id == null) return null;
        return loadedObjects.get(id);
    }

    public abstract void shutdown();

}
