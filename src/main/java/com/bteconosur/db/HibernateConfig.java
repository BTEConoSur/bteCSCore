package com.bteconosur.db;

import java.util.List;
import java.util.Properties;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public class HibernateConfig {

    private final YamlConfiguration config;
    private final YamlConfiguration lang;

    private final ComponentLogger logger;

    private static SessionFactory sessionFactory;

    public HibernateConfig() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        lang = configHandler.getLang();
        logger = BTEConoSur.getInstance().getComponentLogger();

        logger.info(lang.getString("hibernate-initializing"));
    }


    public synchronized SessionFactory buildSessionFactory(List<Class<?>> entities) {
        if (sessionFactory != null) {
            logger.warn("SessionFactory ya está inicializado. Usar getSessionFactory().");
            return sessionFactory;
        }

        Properties settings = new Properties();
        settings.put("connection.driver_class", config.getString("database-driver"));
        settings.put("connection.url", config.getString("database-url"));
        settings.put("connection.username", config.getString("database-username"));
        settings.put("connection.password", config.getString("database-password"));
        settings.put("dialect", config.getString("database-dialect"));
        settings.put("show_sql", config.getString("database-show-sql"));
        settings.put("current_session_context_class", config.getString("database-current-session-context-class"));
        settings.put("hibernate.hbm2ddl.auto", "update"); // TODO: Ajustar según entorno (development/production)

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        try {
            MetadataSources sources = new MetadataSources(registry);
            for (Class<?> entity : entities) {
                sources.addAnnotatedClass(entity);
            }

            sessionFactory = sources.buildMetadata().buildSessionFactory();
            return sessionFactory;
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            logger.error("Error al construir SessionFactory de Hibernate", e);
            return null;
        }
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            logger.error("SessionFactory no inicializado.");
            return null;
        }
        return sessionFactory;
    }

    public void shutdown() {
        if (sessionFactory != null) {
            logger.info(lang.getString("hibernate-shutting-down"));
            sessionFactory.close();
        } else {
            logger.warn("SessionFactory no estaba inicializado al intentar cerrarlo.");
        }
    }
}
