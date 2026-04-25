package com.bteconosur.core.api;

import java.io.Console;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.api.json.bteweb.Claim;
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
     * @return claim creado, o null si ocurre un error.
     */
    public Claim createClaim(Proyecto proyecto) {
        if (proyecto == null) {
            ConsoleLogger.warn("No se pudo crear claim: proyecto nulo.");
            return null;
        }

        ClaimRequest claimRequest = ApiUtils.toClaimRequest(proyecto);

        String endpointTemplate = config.getString("web-claim-create-url", "");

        String token = ApiUtils.getToken(proyecto.getPais());
        String buildTeamId = ApiUtils.getBuildTeamId(proyecto.getPais());

        if (token.isBlank()) {
            ConsoleLogger.error("No se encontró token para crear claim (debug o país).");
            return null;
        }

        if (buildTeamId.isBlank()) {
            ConsoleLogger.error("No se encontró buildTeamId/slug para crear claim (debug o país).");
            return null;
        }

        String urlStr = endpointTemplate.replace("%buildteam%", buildTeamId);
        ConsoleLogger.debug("Claim Request:", claimRequest);
        ConsoleLogger.debug("Usando URL: " + urlStr);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(mapper.writeValueAsBytes(claimRequest));
            }

            int status = conn.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
            String body = ApiUtils.readBody(stream);
            if (status < 200 || status >= 300) {
                try {
                    Error apiError = mapper.readValue(body, Error.class);
                    ConsoleLogger.error("Error creando claim. HTTP " + status + ": " + apiError.getMessage());
                } catch (Exception ignored) {
                    ConsoleLogger.error("Error creando claim. HTTP " + status + ": " + body);
                }
                return null;
            }

            return mapper.readValue(body, Claim.class);
        } catch (Exception e) {
            ConsoleLogger.error("Error al crear claim en la API web: ", e);
            return null;
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
