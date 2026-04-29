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

    public void updateClaim(Proyecto proyecto) {
        if (!config.getBoolean("api-manager-enabled")) return;
        if (proyecto == null) {
            ConsoleLogger.warn("No se pudo actualizar claim: proyecto nulo.");
            return;
        }

        sendClaimAsync(proyecto, "web-claim-update-url", "PUT", "actualizar", true);
    }

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
        if (endpointTemplate == null || endpointTemplate.isBlank()) {
            ConsoleLogger.error("No se encontró endpoint para " + operationLabel + " claim: " + endpointKey);
            return false;
        }

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
            ConsoleLogger.debug("Claim Request:", claimRequest);
        }
        ConsoleLogger.debug("Usando URL para " + operationLabel + ": " + urlStr);
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
        } catch (Exception e) {
            ConsoleLogger.warn("Error al " + operationLabel + " claim en la API web: ", e);
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
                    success = sendClaim(proyecto, "web-claim-create-url", "POST", "crear", true);
                    pendingCreate.remove(id);
                } else if (isUpdate) {
                    success = sendClaim(proyecto, "web-claim-update-url", "PUT", "actualizar", true);
                    pendingUpdate.remove(id);
                } else if (isDelete) {
                    success = sendClaim(proyecto, "web-claim-delete-url", "DELETE", "eliminar", false);
                    pendingDelete.remove(id);
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

    

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("api-manager-shutting-down"));
        if (syncTask != null) {
            pending.set("create", new ArrayList<>(pendingCreate));
            pending.set("update", new ArrayList<>(pendingUpdate));
            pending.set("delete", new ArrayList<>(pendingDelete));
            ConfigHandler.getInstance().save();
            syncTask.cancel();
        }
        if (instance != null) {
            instance = null;
        }
    }

    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

}
