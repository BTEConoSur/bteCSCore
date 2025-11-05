package com.bteconosur.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.reflections.Reflections;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.utils.ConsoleLogger;
import com.bteconosur.core.utils.PluginRegistry;

import jakarta.persistence.Entity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;

public class DBManager {

    private final YamlConfiguration lang;
    private final ConsoleLogger logger;

    private HibernateConfig hibernateConfig;

    private SessionFactory sessionFactory;

    public DBManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        logger = BTEConoSur.getConsoleLogger();

        logger.info(lang.getString("database-initializing"));

        List<Class<?>> entityClasses = new ArrayList<>();
        Reflections reflections = new Reflections("com.bteconosur.db.model");
        entityClasses.addAll(reflections.getTypesAnnotatedWith(Entity.class));

        hibernateConfig = new HibernateConfig();
        sessionFactory = hibernateConfig.buildSessionFactory(entityClasses);
        if (sessionFactory == null)
            PluginRegistry.disablePlugin("La inicialización de la Base de Datos falló."); // TODO: Posiblemente cambiar cuando se implemente la protección de mundos.
    }

    public void executeTransaction(Consumer<Session> action) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            action.accept(session); 
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Excepción en transacción de Hibernate: " + e); // TODO: Mejorar muestreo de excepciones
        }
    }

    public <R> R executeQuery(Function<Session, R> action) {
        try (Session session = sessionFactory.openSession()) {
            return action.apply(session);
        } catch (Exception e) {
            logger.error("Excepción en consulta de Hibernate: " + e);
            return null; 
        }
    }

    public void save(Object entity) {
        executeTransaction(session -> session.persist(entity));
    }

    public void merge(Object entity) {
        executeTransaction(session -> session.merge(entity));
    }

    public void remove(Object entity) {
        executeTransaction(session -> session.remove(entity));
    }

    public <T> T get(Class<T> type, Serializable id) {
        return executeQuery(session -> session.find(type, id));
    }

    public <T> boolean exists(Class<T> type, Serializable id) {
        return get(type, id) != null;
    }

    public <T> List<T> selectAll(Class<T> type) {
        return executeQuery(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
            criteriaQuery.from(type);

            return session.createQuery(criteriaQuery).getResultList();
        });
    }

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

    public <T> List<T> findByProperty(Class<T> type, String propertyName, Object value) {
        return executeQuery(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
            
            Root<T> root = criteriaQuery.from(type);
            
            criteriaQuery.where(builder.equal(root.get(propertyName), value));
            
            return session.createQuery(criteriaQuery).getResultList();
        });
    }

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

    public <T> List<T> executeHQLQuery(String hql, Class<T> resultType) {
        return executeQuery(session -> {
            Query<T> query = session.createQuery(hql, resultType);
            return query.list();
        });
    }

    public List<Object[]> executeSQLQuery(String sql) {
         return executeQuery(session -> {
            return session.createNativeQuery(sql, Object[].class).list();
        });
    }


    public void shutdown() {
        logger.info(lang.getString("database-shutting-down"));
        hibernateConfig.shutdown();
        if (sessionFactory != null) {
            sessionFactory = null;
        }
    }
}
