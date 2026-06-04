package com.bteconosur.core.command.btecs.test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.SatMapUtils;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.ProyectoRegistry;

/**
 * Comando de script que se ejecuta en segundo plano verificando
 * si hay proyectos pendientes (EN_CREACION) y descargando sus imágenes.
 */
public class ScriptCheckImagesCommand extends BaseCommand {

    public ScriptCheckImagesCommand() {
        super("checkimages", null, "btecs.command.btecs.test", CommandMode.CONSOLE_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            String proyectoId = args[0];
            try {
                Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);

                if (proyecto == null) {
                    PlayerLogger.warn(sender, "Proyecto no encontrado: " + proyectoId, (String) null);
                    return false;
                }

                File projectsFolder = new File(BTEConoSur.getInstance().getDataFolder(), "images/projects");
                if (!projectsFolder.exists()) {
                    projectsFolder.mkdirs();
                }

                File imageFile = new File(projectsFolder, proyectoId + ".png");
                if (imageFile.exists()) {
                    imageFile.delete();
                }

                File downloadedImage = SatMapUtils.downloadImage(proyecto);
                if (downloadedImage != null) {
                    PlayerLogger.info(sender, "Imagen descargada: " + proyectoId, (String) null);
                } else {
                    PlayerLogger.warn(sender, "No se pudo descargar: " + proyectoId, (String) null);
                }
            } catch (Exception e) {
                PlayerLogger.warn(sender, "Error descargando imagen: " + e.getMessage(), (String) null);
            }
            return true;
        }

        Set<String> allProjects = new HashSet<>();
        for (Proyecto proyecto : ProyectoRegistry.getInstance().getList()) {
            allProjects.add(proyecto.getId());
        }

        if (allProjects.isEmpty()) {
            PlayerLogger.info(sender, "No hay proyectos disponibles.", (String) null);
            return true;
        }

        PlayerLogger.info(sender, "Script iniciado. Total de proyectos: " + allProjects.size(), (String) null);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (allProjects.isEmpty()) {
                    ConsoleLogger.info("Script finalizado. Todos los proyectos procesados.");
                    cancel();
                    return;
                }

                try {
                    File projectsFolder = new File(BTEConoSur.getInstance().getDataFolder(), "images/projects");

                    String proyectoId = allProjects.iterator().next();
                    Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);

                    if (proyecto == null) {
                        allProjects.remove(proyectoId);
                        //ConsoleLogger.debug("Proyecto removido: " + proyectoId);
                        return;
                    }

                    File imageFile = new File(projectsFolder, proyectoId + ".png");

                    if (!imageFile.exists()) {
                        try {
                            File downloadedImage = SatMapUtils.downloadImage(proyecto);
                            if (downloadedImage != null && downloadedImage.exists()) {
                                ConsoleLogger.debug("Imagen descargada para: " + proyectoId);
                            }
                        } catch (Exception e) {
                            ConsoleLogger.warn("No se pudo descargar imagen para " + proyectoId);
                        }
                    }

                    allProjects.remove(proyectoId);
                    ConsoleLogger.debug("Proyectos restantes: " + allProjects.size());

                } catch (Exception e) {
                    ConsoleLogger.error("Error en script: ", e);
                }
            }
        }.runTaskTimerAsynchronously(BTEConoSur.getInstance(), 0L, 100L);
        return true;
    }

}
