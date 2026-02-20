package com.bteconosur.db;

import java.util.List;
import java.util.Properties;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;


public class HibernateConfig {

    private final YamlConfiguration config;
    private final YamlConfiguration secret;

    private static SessionFactory sessionFactory;

    public HibernateConfig() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        secret = configHandler.getSecret();
        ConsoleLogger.info(LanguageHandler.getText("hibernate-initializing"));
    }


    public synchronized SessionFactory buildSessionFactory(List<Class<?>> entities) {
        if (sessionFactory != null) {
            ConsoleLogger.warn("SessionFactory ya está inicializado. Usar getSessionFactory().");
            return sessionFactory;
        }

        Properties settings = new Properties();
        settings.put("hibernate.connection.driver_class", config.getString("database-driver"));
        settings.put("hibernate.connection.url", secret.getString("database-url"));
        settings.put("hibernate.connection.username", secret.getString("database-username"));
        settings.put("hibernate.connection.password", secret.getString("database-password"));
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
            ConsoleLogger.error(LanguageHandler.getText("database-connection-error") + e);
            return null;
        }
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            ConsoleLogger.error(LanguageHandler.getText("database-not-connected"));
            return null;
        }
        return sessionFactory;
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("hibernate-shutting-down"));
        if (sessionFactory == null) {
            ConsoleLogger.warn(LanguageHandler.getText("database-not-connected"));
            return;
        }
        sessionFactory.close();
        sessionFactory = null;
    }
}
