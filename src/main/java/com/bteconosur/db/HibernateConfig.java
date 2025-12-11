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
import com.bteconosur.core.util.ConsoleLogger;


public class HibernateConfig {

    private final YamlConfiguration config;
    private final YamlConfiguration lang;

    private final ConsoleLogger logger;

    private static SessionFactory sessionFactory;

    public HibernateConfig() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        lang = configHandler.getLang();
        logger = BTEConoSur.getConsoleLogger();

        logger.info(lang.getString("hibernate-initializing"));
    }


    public synchronized SessionFactory buildSessionFactory(List<Class<?>> entities) {
        if (sessionFactory != null) {
            logger.warn("SessionFactory ya está inicializado. Usar getSessionFactory().");
            return sessionFactory;
        }

        Properties settings = new Properties();
        settings.put("hibernate.connection.driver_class", config.getString("database-driver"));
        settings.put("hibernate.connection.url", config.getString("database-url"));
        settings.put("hibernate.connection.username", config.getString("database-username"));
        settings.put("hibernate.connection.password", config.getString("database-password"));
        settings.put("hibernate.show_sql", config.getString("database-show-sql"));
        settings.put("hibernate.current_session_context_class", config.getString("database-current-session-context-class"));
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
            logger.error("Excepción al construir SessionFactory de Hibernate: " + e);
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

    public void shutdown() { // TODO: Ver si coinciden shutdowns de DBManager y DiscordManager
        logger.info(lang.getString("hibernate-shutting-down"));
        if (sessionFactory == null) {
            logger.warn("Hibernate no está inicializado.");
            return;
        }
        sessionFactory.close();
        sessionFactory = null;
    }
}
