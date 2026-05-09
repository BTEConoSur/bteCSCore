package com.bteconosur.core.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.api.json.bteweb.ClaimRequest;
import com.bteconosur.core.api.json.bteweb.Error;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Gestor centralizado para la sincronización de reclamaciones (claims) entre el servidor Minecraft
 * y la API web de BTE (Build the Earth). Administra operaciones de creación, actualización y eliminación
 * de claims, con soporte para sincronización asincrónica y gestión de operaciones pendientes.
 */
public class ApiManager {

    private static ApiManager instance;
    private static final ObjectMapper mapper = new ObjectMapper();
    private final YamlConfiguration config;
    private final YamlConfiguration pending;

    private Set<String> pendingCreate = ConcurrentHashMap.newKeySet();
    private Set<String> pendingUpdate = ConcurrentHashMap.newKeySet();
    private Set<String> pendingDelete = ConcurrentHashMap.newKeySet();

    private BukkitTask syncTask;
    private BukkitTask loadTask;
    private BukkitTask syncAllTask;
    
    /**
     * Inicializa el gestor de API cargando la configuración y estableciendo las tareas de sincronización
     * si están habilitadas en el archivo de configuración.
     */
    public ApiManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        pending = configHandler.getPending();
        if (!config.getBoolean("api-manager-enabled")) return;
        ConsoleLogger.info(LanguageHandler.getText("api-manager-initializing"));
        if (config.getBoolean("web-pending-sync-enabled")) initSyncTask();
    }

    /**
     * Crea un claim en la API web de BTE usando coordType=array.
     * Si web-debug-mode está activo usa credenciales debug; si no, usa token e ID por país.
     *
     * @param proyecto proyecto del cual se infiere el país para resolver credenciales.
     */
    public void createClaim(Proyecto proyecto) {
        if (!config.getBoolean("api-manager-enabled")) return;
        if (proyecto == null) {
            ConsoleLogger.warn("No se pudo crear claim: proyecto nulo.");
            return;
        }
        sendClaimAsync(proyecto, "web-claim-create-url", "POST", "crear", true);
    }

    /**
     * Actualiza un claim existente en la API web de BTE.
     * Si web-debug-mode está activo usa credenciales debug; si no, usa token e ID por país.
     *
     * @param proyecto proyecto cuyo claim será actualizado.
     */
    public void updateClaim(Proyecto proyecto) {
        if (!config.getBoolean("api-manager-enabled")) return;
        if (proyecto == null) {
            ConsoleLogger.warn("No se pudo actualizar claim: proyecto nulo.");
            return;
        }

        sendClaimAsync(proyecto, "web-claim-update-url", "PUT", "actualizar", true);
    }

    /**
     * Elimina un claim existente de la API web de BTE.
     * Si web-debug-mode está activo usa credenciales debug; si no, usa token e ID por país.
     *
     * @param proyecto proyecto cuyo claim será eliminado.
     */
    public void deleteClaim(Proyecto proyecto) {
        if (!config.getBoolean("api-manager-enabled")) return;
        if (proyecto == null) {
            ConsoleLogger.warn("No se pudo eliminar claim: proyecto nulo.");
            return;
        }

        sendClaimAsync(proyecto, "web-claim-delete-url", "DELETE", "eliminar", false);
    }

    private void sendClaimAsync(Proyecto proyecto, String endpointKey, String method, String operationLabel, boolean includeBody) {
        Bukkit.getScheduler().runTaskAsynchronously(BTEConoSur.getInstance(), () ->
            sendClaim(proyecto, endpointKey, method, operationLabel, includeBody)
        );
    }

    @SuppressWarnings("deprecation")
    private boolean sendClaim(Proyecto proyecto, String endpointKey, String method, String operationLabel, boolean includeBody) {
        String endpointTemplate = config.getString(endpointKey);

        ClaimRequest claimRequest = includeBody ? ApiUtils.toClaimRequest(proyecto) : null;

        String token = ApiUtils.getToken(proyecto.getPais());
        String buildTeamId = ApiUtils.getBuildTeamId(proyecto.getPais());

        if (token.isBlank()) {
            ConsoleLogger.error("No se encontró token para " + operationLabel + " claim (debug o país).");
            return false;
        }

        if (buildTeamId.isBlank()) {
            ConsoleLogger.error("No se encontró buildTeamId/slug para " + operationLabel + " claim (debug o país).");
            return false;
        }

        String urlStr = endpointTemplate.replace("%buildteam%", buildTeamId).replace("%claimid%", proyecto.getId());
        if (includeBody && claimRequest != null) {
           //ConsoleLogger.debug("Claim Request:", claimRequest);
        }
        //ConsoleLogger.debug("Usando URL para " + operationLabel + ": " + urlStr);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(config.getInt("web-claim-connect-timeout"));
            conn.setReadTimeout(config.getInt("web-claim-read-timeout"));
            conn.setDoOutput(includeBody);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/json");
            if (includeBody) {
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(mapper.writeValueAsBytes(claimRequest));
                }
            }

            int status = conn.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
            String body = ApiUtils.readBody(stream);
            if (status < 200 || status >= 300) {
                Error apiError = mapper.readValue(body, Error.class);
                ConsoleLogger.warn("Error al " + operationLabel + " claim. HTTP " + status + ": " + apiError.getMessage());
                switch (operationLabel) {
                    case "crear" -> {
                        if (!apiError.getMessage().contains("externalId")) pendingCreate.add(proyecto.getId());
                        else return true;
                    }
                    case "actualizar" -> {
                        if (status == 404) {
                            ConsoleLogger.warn("Claim no encontrado para actualizar.");
                            pendingCreate.add(proyecto.getId());
                        } else pendingUpdate.add(proyecto.getId());
                    }
                    case "eliminar" -> {
                        if (status != 404) pendingDelete.add(proyecto.getId());
                        else return true;
                    }
                }
                
                return false;
            }
            return true;
        } catch (SocketTimeoutException e) {
            ConsoleLogger.warn("Timeout al " + operationLabel + " claim en la API web.");
            switch (operationLabel) {
                case "crear" -> pendingCreate.add(proyecto.getId());
                case "actualizar" -> pendingUpdate.add(proyecto.getId());
                case "eliminar" -> pendingDelete.add(proyecto.getId());
            }
        } catch (Exception e) {
            ConsoleLogger.warn("Error al " + operationLabel + " claim en la API web: ", e);
            switch (operationLabel) {
                case "crear" -> pendingCreate.add(proyecto.getId());
                case "actualizar" -> pendingUpdate.add(proyecto.getId());
                case "eliminar" -> pendingDelete.add(proyecto.getId());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return false;
    }

    private void initSyncTask() {
        long intervalHours = config.getLong("web-pending-sync-interval");
        long intervalTicks = intervalHours * 60 * 60 * 20;

        pendingCreate.addAll(pending.getStringList("create"));
        pendingUpdate.addAll(pending.getStringList("update"));
        pendingDelete.addAll(pending.getStringList("delete"));

        ConsoleLogger.debug("Create pending: " + pendingCreate);
        ConsoleLogger.debug("Update pending: " + pendingUpdate);
        ConsoleLogger.debug("Delete pending: " + pendingDelete);

        syncTask = Bukkit.getScheduler().runTaskTimerAsynchronously(BTEConoSur.getInstance(), () -> {
            ConsoleLogger.info(LanguageHandler.getText("web-sync-task-init"));
            if (pendingCreate.isEmpty() && pendingUpdate.isEmpty() && pendingDelete.isEmpty()) {
                ConsoleLogger.info(LanguageHandler.getText("web-sync-no-pending"));
                return;
            }
            if (loadTask != null) {
                return;
            }
            final int[] consecutiveFailures = {0};
            final Set<String> createSnapshot = new LinkedHashSet<>(pendingCreate);
            final Set<String> updateSnapshot = new LinkedHashSet<>(pendingUpdate);
            final Set<String> deleteSnapshot = new LinkedHashSet<>(pendingDelete);
            pendingCreate.removeAll(createSnapshot);
            pendingUpdate.removeAll(updateSnapshot);
            pendingDelete.removeAll(deleteSnapshot);
            pending.set("create", new ArrayList<>(pendingCreate));
            pending.set("update", new ArrayList<>(pendingUpdate));
            pending.set("delete", new ArrayList<>(pendingDelete));
            ConsoleLogger.debug("Create pending: " + createSnapshot);
            ConsoleLogger.debug("Update pending: " + updateSnapshot);
            ConsoleLogger.debug("Delete pending: " + deleteSnapshot);
            ConfigHandler.getInstance().save();
                        
            loadTask = Bukkit.getScheduler().runTaskTimerAsynchronously(BTEConoSur.getInstance(), () -> {
                String id = null;
                boolean isCreate = false, isUpdate = false, isDelete = false;

                id = createSnapshot.stream().findAny().orElse(null);
                if (id != null) {
                    createSnapshot.remove(id);
                    isCreate = true;
                } else {
                    id = updateSnapshot.stream().findAny().orElse(null);
                    if (id != null) {
                        updateSnapshot.remove(id);
                        isUpdate = true;
                    } else {
                        id = deleteSnapshot.stream().findAny().orElse(null);
                        if (id != null) {
                            deleteSnapshot.remove(id);
                            isDelete = true;
                        }
                    }
                }

                if (id == null) {
                    loadTask.cancel();
                    loadTask = null;
                    return;
                }

                Proyecto proyecto = ProyectoRegistry.getInstance().get(id);
                if (proyecto == null) return;

                boolean success = false;
                if (isCreate) {
                    pendingCreate.remove(id);
                    success = sendClaim(proyecto, "web-claim-create-url", "POST", "crear", true);
                } else if (isUpdate) {
                    pendingUpdate.remove(id);
                    success = sendClaim(proyecto, "web-claim-update-url", "PUT", "actualizar", true);
                } else if (isDelete) {
                    pendingDelete.remove(id);
                    success = sendClaim(proyecto, "web-claim-delete-url", "DELETE", "eliminar", false);    
                }

                if (success) {
                    consecutiveFailures[0] = 0;
                } else {
                    consecutiveFailures[0]++;
                    if (consecutiveFailures[0] > config.getInt("web-consecutive-pending-failures")) {
                        ConsoleLogger.warn("Se canceló la subida de pendientes: " + config.getInt("web-consecutive-pending-failures") + " tareas fallaron de forma consecutiva.");
                        loadTask.cancel();
                    }
                }

            }, 0L,  config.getLong("web-load-interval") * 20);

        }, 20 * 60 * 5, intervalTicks);
    }

    @SuppressWarnings("deprecation")
    private Boolean claimExists(Proyecto proyecto) {
        String endpointTemplate = config.getString("web-claim-get-url");

        String token = ApiUtils.getToken(proyecto.getPais());
        String buildTeamId = ApiUtils.getBuildTeamId(proyecto.getPais());
        if (token.isBlank() || buildTeamId.isBlank()) {
            ConsoleLogger.warn("No se pudo consultar existencia del claim por credenciales incompletas.");
            return null;
        }

        String urlStr = endpointTemplate.replace("%buildteam%", buildTeamId).replace("%claimid%", proyecto.getId());
        HttpURLConnection conn = null;
        ConsoleLogger.debug("Usando URL para consultar claim: " + urlStr);
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(config.getInt("web-claim-connect-timeout"));
            conn.setReadTimeout(config.getInt("web-claim-read-timeout"));
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                ConsoleLogger.debug("Claim existe: " + proyecto.getId());
                return true;
            }
            if (status == 404) {
                ConsoleLogger.debug("Claim no existe: " + proyecto.getId());
                return false;
            }

            String body = ApiUtils.readBody(conn.getErrorStream());
            ConsoleLogger.warn("No se pudo confirmar existencia del claim " + proyecto.getId() + ". HTTP " + status + ": " + body);
            return null;
        } catch (Exception e) {
            ConsoleLogger.warn("Error al consultar existencia del claim " + proyecto.getId() + ": ", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void logProgressBar(int processed, int total, int[] nextMilestone) {
        if (total <= 0) return;
        int percent = (processed * 100) / total;
        while (nextMilestone[0] <= 100 && percent >= nextMilestone[0]) {
            int filled = nextMilestone[0] / 10;
            String bar = "[" + "#".repeat(filled) + "-".repeat(10 - filled) + "]";
            ConsoleLogger.info("Sync web progreso " + bar + " " + nextMilestone[0] + "% (" + processed + "/" + total + ")");
            nextMilestone[0] += 10;
        }
    }

    private boolean syncClaimByState(Proyecto proyecto) {
        if (proyecto == null) {
            return false;
        }

        Boolean exists = claimExists(proyecto);
        if (exists == null) {
            ConsoleLogger.warn("No se pudo determinar si el claim existe para " + proyecto.getId() + ". Se agrega a pendientes.");
            pendingCreate.add(proyecto.getId());
            return true;
        }

        if (exists) {
            return sendClaim(proyecto, "web-claim-update-url", "PUT", "actualizar", true);
        }

        return sendClaim(proyecto, "web-claim-create-url", "POST", "crear", true);
    }

    /**
     * Sincroniza un proyecto individual con la API web, verificando si el claim existe y realizando
     * la operación correspondiente (crear o actualizar).
     *
     * @param proyectoId identificador del proyecto a sincronizar.
     */
    public void syncProject(String proyectoId) {
        if (!config.getBoolean("api-manager-enabled")) return;

        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId.trim());
        if (proyecto == null) {
            ConsoleLogger.warn("No se encontró el proyecto " + proyectoId + " en el registry.");
            return;
        }

        ConsoleLogger.info("Iniciando sincronización web del proyecto " + proyecto.getId() + "...");
        Bukkit.getScheduler().runTaskAsynchronously(BTEConoSur.getInstance(), () -> {
            if (syncClaimByState(proyecto)) {
                ConsoleLogger.info("Sincronización web del proyecto " + proyecto.getId() + " completa.");
            } else {
                ConsoleLogger.warn("Sincronización web del proyecto " + proyecto.getId() + " no pudo completarse.");
            }
        });
    }

    /**
     * Sincroniza todos los proyectos registrados con la API web de forma asincrónica.
     * Cada proyecto se procesa uno a la vez con intervalos configurables, y se muestra una barra
     * de progreso en la consola cada vez que se completa un 10% del progreso total.
     */
    public void syncAll() {
        if (!config.getBoolean("api-manager-enabled")) return;
        if (syncAllTask != null) {
            ConsoleLogger.warn("Ya hay una sincronización completa en curso.");
            return;
        }

        final Set<String> updateSnapshot = ProyectoRegistry.getInstance().getList().stream()
            .map(Proyecto::getId)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (updateSnapshot.isEmpty()) {
            ConsoleLogger.info("No hay proyectos para sincronizar con la web.");
            return;
        }

        final int total = updateSnapshot.size();
        final int[] processed = {0};
        final int[] nextMilestone = {0};
        final long intervalTicks = config.getLong("web-load-interval") * 20;

        ConsoleLogger.info("Iniciando sincronización web de " + total + " proyectos...");
        logProgressBar(0, total, nextMilestone);
        syncAllTask = Bukkit.getScheduler().runTaskTimerAsynchronously(BTEConoSur.getInstance(), () -> {
            String id = updateSnapshot.stream().findAny().orElse(null);
            if (id == null) {
                if (nextMilestone[0] <= 100) {
                    logProgressBar(total, total, nextMilestone);
                }
                ConsoleLogger.info("Sincronización web completa.");
                syncAllTask.cancel();
                syncAllTask = null;
                return;
            }

            updateSnapshot.remove(id);
            Proyecto proyecto = ProyectoRegistry.getInstance().get(id);
            if (proyecto == null) {
                processed[0]++;
                logProgressBar(processed[0], total, nextMilestone);
                return;
            }

            syncClaimByState(proyecto);

            processed[0]++;
            logProgressBar(processed[0], total, nextMilestone);
        }, 0L, intervalTicks);
    }

    /**
     * Detiene el gestor de API guardando el estado de operaciones pendientes, cancelando todas las tareas
     * asincrónicas en ejecución y liberando recursos. Debe ser llamado durante el apagado del servidor.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("api-manager-shutting-down"));
        if (syncTask != null) {
            pending.set("create", new ArrayList<>(pendingCreate));
            pending.set("update", new ArrayList<>(pendingUpdate));
            pending.set("delete", new ArrayList<>(pendingDelete));
            ConfigHandler.getInstance().save();
            syncTask.cancel();
        }
        if (loadTask != null) {
            loadTask.cancel();
        }
        if (syncAllTask != null) {
            syncAllTask.cancel();
        }
        if (instance != null) {
            instance = null;
        }
    }

    /**
     * Obtiene la instancia única del gestor de API (patrón Singleton).
     * Si la instancia aún no ha sido creada, la crea automáticamente.
     *
     * @return instancia única del gestor de API.
     */
    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

}
