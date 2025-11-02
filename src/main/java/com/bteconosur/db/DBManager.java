package com.bteconosur.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hibernate.SessionFactory;
import org.reflections.Reflections;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.utils.PluginRegistry;

import jakarta.persistence.Entity;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public class DBManager {

    private final YamlConfiguration config;
    private final YamlConfiguration lang;

    private final ComponentLogger logger;

    private HibernateConfig hibernateConfig;

    private SessionFactory sessionFactory;

    public DBManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        lang = configHandler.getLang();
        logger = BTEConoSur.getInstance().getComponentLogger();

        logger.info(lang.getString("database-initializing"));

        List<Class<?>> entityClasses = new ArrayList<>();
        Reflections reflections = new Reflections("com.bteconosur.db.model");
        entityClasses.addAll(reflections.getTypesAnnotatedWith(Entity.class));

        hibernateConfig = new HibernateConfig();
        sessionFactory = hibernateConfig.buildSessionFactory(entityClasses);
        if (sessionFactory == null)
            PluginRegistry.disablePlugin("La inicialización de la Base de Datos falló."); // TODO: Posiblemente cambiar cuando se implemente la protección de mundos.
    }



    public void shutdown() {
        if (sessionFactory != null) {
            logger.info(lang.getString("database-shutting-down"));
            hibernateConfig.shutdown();
        }
    }
}
