package com.bteconosur.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PluginRegistry;

import jakarta.persistence.Entity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;

/**
 * Gestor de base de datos del plugin.
 * Proporciona métodos para realizar operaciones CRUD y consultas sobre las entidades de la base de datos.
 * Utiliza Hibernate como capa de abstracción y maneja automáticamente las transacciones y sesiones.
 */
public class DBManager {
    
    private static DBManager instance;

    private HibernateConfig hibernateConfig;

    private SessionFactory sessionFactory;

    /**
     * Constructor del gestor de base de datos.
     * Inicializa Hibernate, escanea las entidades del modelo y construye la SessionFactory.
     */
    public DBManager() {
        ConsoleLogger.info(LanguageHandler.getText("database-initializing"));

        List<Class<?>> entityClasses = new ArrayList<>();
        ClassLoader libsClassLoader = getClass().getClassLoader();

        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("com.bteconosur.db.model", libsClassLoader))
                .addClassLoaders(libsClassLoader, getClass().getClassLoader())
                .filterInputsBy(new FilterBuilder().includePackage("com.bteconosur.db.model"))
        );
        entityClasses.addAll(reflections.getTypesAnnotatedWith(Entity.class));

        hibernateConfig = new HibernateConfig();
        sessionFactory = hibernateConfig.buildSessionFactory(entityClasses);
        if (sessionFactory == null)
            PluginRegistry.disablePlugin("DATABASE_LOAD_ERROR.");
    }

    /**
     * Ejecuta una transacción de base de datos con manejo automático de commit y rollback.
     * 
     * @param action Acción a ejecutar dentro de la transacción
     */
    public void executeTransaction(Consumer<Session> action) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                action.accept(session); 
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("database-transaction-error"), e);
        }
    }

    /**
     * Ejecuta una consulta de solo lectura sobre la base de datos.
     * 
     * @param <R> Tipo de resultado de la consulta
     * @param action Función que ejecuta la consulta y retorna el resultado
     * @return El resultado de la consulta, o null si ocurre un error
     */
    public <R> R executeQuery(Function<Session, R> action) {
        try (Session session = sessionFactory.openSession()) {
            return action.apply(session);
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("database-query-error"), e);
            return null; 
        }
    }

    /**
     * Persiste una nueva entidad en la base de datos.
     * 
     * @param entity Entidad a persistir
     */
    public void save(Object entity) {
        executeTransaction(session -> session.persist(entity));
    }

    /**
     * Actualiza una entidad existente en la base de datos.
     * 
     * @param entity Entidad a actualizar
     * @return La entidad actualizada y gestionada por Hibernate
     */
    public Object merge(Object entity) {
        final Object[] merged = new Object[1];
        executeTransaction(session -> merged[0] = session.merge(entity));
        return merged[0];
    }

    /**
     * Elimina una entidad de la base de datos.
     * 
     * @param entity Entidad a eliminar
     */
    public void remove(Object entity) {
        executeTransaction(session -> session.remove(entity));
    }

    /**
     * Obtiene una entidad por su identificador.
     * 
     * @param <T> Tipo de la entidad
     * @param type Clase de la entidad
     * @param id Identificador de la entidad
     * @return La entidad encontrada, o null si no existe
     */
    public <T> T get(Class<T> type, Serializable id) {
        return executeQuery(session -> session.find(type, id));
    }

    /**
     * Verifica si existe una entidad con el identificador especificado.
     * 
     * @param <T> Tipo de la entidad
     * @param type Clase de la entidad
     * @param id Identificador de la entidad
     * @return true si la entidad existe, false en caso contrario
     */
    public <T> boolean exists(Class<T> type, Serializable id) {
        return get(type, id) != null;
    }

    /**
     * Obtiene todas las entidades de un tipo específico.
     * 
     * @param <T> Tipo de la entidad
     * @param type Clase de la entidad
     * @return Lista con todas las entidades del tipo especificado
     */
    public <T> List<T> selectAll(Class<T> type) {
        return executeQuery(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
            criteriaQuery.from(type);

            return session.createQuery(criteriaQuery).getResultList();
        });
    }

    /**
     * Obtiene todas las entidades de un tipo específico con paginación.
     * 
     * @param <T> Tipo de la entidad
     * @param type Clase de la entidad
     * @param page Número de página (1-indexed)
     * @param pageSize Cantidad de resultados por página
     * @return Lista con las entidades de la página solicitada
     */
    public <T> List<T> selectAllPaged(Class<T> type, int page, int pageSize) {
    return executeQuery(session -> {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
        criteriaQuery.from(type);
        
        return session.createQuery(criteriaQuery)
                     .setFirstResult((page - 1) * pageSize)
                     .setMaxResults(pageSize)
                     .getResultList();
    });
}

    /**
     * Busca entidades por el valor de una propiedad específica.
     * 
     * @param <T> Tipo de la entidad
     * @param type Clase de la entidad
     * @param propertyName Nombre de la propiedad
     * @param value Valor de la propiedad
     * @return Lista de entidades que coinciden con el criterio
     */
    public <T> List<T> findByProperty(Class<T> type, String propertyName, Object value) {
        return executeQuery(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
            
            Root<T> root = criteriaQuery.from(type);
            
            criteriaQuery.where(builder.equal(root.get(propertyName), value));
            
            return session.createQuery(criteriaQuery).getResultList();
        });
    }

    /**
     * Busca entidades por múltiples propiedades.
     * 
     * @param <T> Tipo de la entidad
     * @param type Clase de la entidad
     * @param properties Mapa con las propiedades y sus valores esperados
     * @return Lista de entidades que coinciden con todos los criterios
     */
    public <T> List<T> findByProperties(Class<T> type, Map<String, Object> properties) {
        return executeQuery(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
            Root<T> root = criteriaQuery.from(type);

            List<Predicate> predicates = new ArrayList<>();
            properties.forEach((propertyName, value) -> 
                predicates.add(builder.equal(root.get(propertyName), value))
            );

            criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));

            return session.createQuery(criteriaQuery).getResultList();
        });
    }

    /**
     * Ejecuta una consulta HQL (Hibernate Query Language).
     * 
     * @param <T> Tipo de resultado
     * @param hql Consulta en formato HQL
     * @param resultType Clase del tipo de resultado
     * @return Lista de resultados de la consulta
     */
    public <T> List<T> executeHQLQuery(String hql, Class<T> resultType) {
        return executeQuery(session -> {
            Query<T> query = session.createQuery(hql, resultType);
            return query.list();
        });
    }

    /**
     * Ejecuta una consulta SQL nativa.
     * 
     * @param sql Consulta en formato SQL
     * @return Lista de arrays de objetos con los resultados de la consulta
     */
    public List<Object[]> executeSQLQuery(String sql) {
         return executeQuery(session -> {
            return session.createNativeQuery(sql, Object[].class).list();
        });
    }

    /**
     * Cierra el gestor de base de datos y libera los recursos de Hibernate.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("database-shutting-down"));
        hibernateConfig.shutdown();
        if (sessionFactory != null) {
            sessionFactory = null;
        }
    }

    /**
     * Obtiene la instancia singleton del gestor de base de datos.
     * 
     * @return La instancia única de DBManager
     */
    public static DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }
}
