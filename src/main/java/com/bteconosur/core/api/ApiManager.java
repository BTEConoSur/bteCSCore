package com.bteconosur.core.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.bukkit.Bukkit;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.api.json.bteweb.ClaimRequest;
import com.bteconosur.core.api.json.bteweb.Error;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Proyecto;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiManager {

    private static ApiManager instance;
    private static final ObjectMapper mapper = new ObjectMapper();
    private final YamlConfiguration config;
    
    public ApiManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();

        ConsoleLogger.info(LanguageHandler.getText("api-manager-initializing"));
    }

    /**
     * Crea un claim en la API web de BTE usando coordType=array.
     * Si web-debug-mode está activo usa credenciales debug; si no, usa token e ID por país.
     *
     * @param proyecto proyecto del cual se infiere el país para resolver credenciales.
     */
    public void createClaim(Proyecto proyecto) {
        if (proyecto == null) {
            ConsoleLogger.warn("No se pudo crear claim: proyecto nulo.");
            return;
        }
        sendClaimAsync(proyecto, "web-claim-create-url", "POST", "crear", true);
    }

    public void updateClaim(Proyecto proyecto) {
        if (proyecto == null) {
            ConsoleLogger.warn("No se pudo actualizar claim: proyecto nulo.");
            return;
        }

        sendClaimAsync(proyecto, "web-claim-update-url", "PUT", "actualizar", true);
    }

    public void deleteClaim(Proyecto proyecto) {
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
    private void sendClaim(Proyecto proyecto, String endpointKey, String method, String operationLabel, boolean includeBody) {
        String endpointTemplate = config.getString(endpointKey);
        if (endpointTemplate == null || endpointTemplate.isBlank()) {
            ConsoleLogger.error("No se encontró endpoint para " + operationLabel + " claim: " + endpointKey);
            return;
        }

        ClaimRequest claimRequest = includeBody ? ApiUtils.toClaimRequest(proyecto) : null;

        String token = ApiUtils.getToken(proyecto.getPais());
        String buildTeamId = ApiUtils.getBuildTeamId(proyecto.getPais());

        if (token.isBlank()) {
            ConsoleLogger.error("No se encontró token para " + operationLabel + " claim (debug o país).");
            return;
        }

        if (buildTeamId.isBlank()) {
            ConsoleLogger.error("No se encontró buildTeamId/slug para " + operationLabel + " claim (debug o país).");
            return;
        }

        String urlStr = endpointTemplate.replace("%buildteam%", buildTeamId).replace("%claimid%", proyecto.getId());
        if (includeBody && claimRequest != null) {
            ConsoleLogger.debug("Claim Request:", claimRequest);
        }
        ConsoleLogger.debug("Usando URL: " + urlStr);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
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
                try {
                    Error apiError = mapper.readValue(body, Error.class);
                    ConsoleLogger.error("Error al " + operationLabel + " claim. HTTP " + status + ": " + apiError.getMessage());
                } catch (Exception ignored) {
                    ConsoleLogger.error("Error al " + operationLabel + " claim. HTTP " + status + ": " + body);
                }
                return;
            }
        } catch (SocketTimeoutException e) {
            ConsoleLogger.error("Timeout al " + operationLabel + " claim en la API web:", e);
        } catch (Exception e) {
            ConsoleLogger.error("Error al " + operationLabel + " claim en la API web: ", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("api-manager-shutting-down"));
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
