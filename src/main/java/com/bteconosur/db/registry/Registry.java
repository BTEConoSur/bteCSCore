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

/**
 * Registro genérico en memoria para entidades persistidas.
 * Proporciona operaciones comunes de carga, lectura, actualización y descarga.
 *
 * @param <K> tipo de la clave identificadora.
 * @param <V> tipo de la entidad registrada.
 */
public abstract class Registry<K extends Serializable, V> {

    protected ConcurrentHashMap<K, V> loadedObjects = new ConcurrentHashMap<>();

    protected final DBManager dbManager;
    protected final YamlConfiguration config;
    
    /**
     * Inicializa el registro con acceso al gestor de base de datos y configuración global.
     */
    public Registry() {
        dbManager = DBManager.getInstance();
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig(); 
    }

    /**
     * Obtiene el mapa interno de objetos cargados.
     *
     * @return mapa de objetos cargados, o mapa vacío si no está inicializado.
     */
    public Map<K, V> getMap() {
        if (loadedObjects == null) return Collections.emptyMap();
        return loadedObjects;
    }

    /**
     * Obtiene una vista en lista de los objetos cargados.
     *
     * @return lista inmutable de objetos cargados, o lista vacía si no está inicializado.
     */
    public List<V> getList() {
        if (loadedObjects == null) return Collections.emptyList();
        return List.copyOf(loadedObjects.values());
    }

    /**
     * Obtiene el conjunto de identificadores cargados.
     *
     * @return conjunto de ids cargados, o conjunto vacío si no está inicializado.
     */
    public Set<K> getIds() {
        if (loadedObjects == null) return Collections.emptySet();
        return new HashSet<>(loadedObjects.keySet());
    }

    /**
     * Verifica si un identificador se encuentra cargado.
     *
     * @param id identificador a verificar.
     * @return {@code true} si el id está en memoria.
     */
    public boolean isLoaded(K id) {
        if (loadedObjects == null) return false;
        return loadedObjects.containsKey(id);
    }

    /**
     * Verifica si un objeto se encuentra cargado.
     *
     * @param obj objeto a verificar.
     * @return {@code true} si el objeto está en memoria.
     */
    public boolean isObjLoaded(V obj) {
        if (loadedObjects == null) return false;
        return loadedObjects.containsValue(obj);
    }
    
    /**
     * Carga una entidad en el registro y en persistencia.
     *
     * @param obj entidad a cargar.
     */
    public abstract void load(V obj);

    /**
     * Guarda una entidad cargada con su estado persistido.
     *
     * @param id identificador de la entidad.
     * @return entidad guardada, o {@code null} si no existe o no se puede guardar.
     */
    @SuppressWarnings("unchecked")
    public V merge(K id) {
        if (id == null || loadedObjects == null) return null;
        V obj = loadedObjects.get(id);
        if (obj == null) return null;
        V mergedObj = (V) dbManager.merge(obj);
        if (mergedObj != null) loadedObjects.put(id, mergedObj);
        return mergedObj;
    }

    /**
     * Descarga una entidad del registro en memoria.
     *
     * @param id identificador de la entidad a remover.
     */
    public void unload(K id) {
        if (id == null || loadedObjects == null) return;
        loadedObjects.remove(id);
    }

    /**
     * Verifica si una entidad existe en el registro en memoria.
     *
     * @param id identificador de la entidad.
     * @return {@code true} si existe en el mapa cargado.
     */
    public boolean exists(K id) {
        if (id == null || loadedObjects == null) return false;
        return loadedObjects.containsKey(id);
    }

    /**
     * Obtiene una entidad por su identificador.
     *
     * @param id identificador de la entidad.
     * @return entidad encontrada, o {@code null} si no existe.
     */
    public V get(K id) {
        if (id == null || loadedObjects == null) return null;
        return loadedObjects.get(id);
    }

    /**
     * Cierra el registro y libera sus recursos en memoria.
     * Debe ser implementado por subclases para realizar tareas de limpieza específicas.
     */
    public abstract void shutdown();

}
