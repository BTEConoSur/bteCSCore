package com.bteconosur.core.util.json;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    
    public static String toJson(Object obj) {
        String json = "";

        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            json = json.replace("\r", "");
        } catch (Exception e) {
            ConsoleLogger.error("Error al serializar objeto a JSON: ", e);
        }

        return json;
    }

    @SuppressWarnings("deprecation")
    public static List<RealLocation> buscar(String query) {
        HttpURLConnection conn = null;
        try {
            String urlStr = config.getString("tpdir-link").replace("%search%", query);
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "BTEConoSurCore/3.0 (Minecraft Plugin)");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Language", config.getString("tpdir-accept-language", "es-AR,es;q=0.9,en;q=0.7"));

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                ConsoleLogger.warn("Error al buscar ubicaciones. HTTP status: " + status);
                return Collections.emptyList();
            }

            try (InputStream input = conn.getInputStream()) {
                RealLocation[] arr = mapper.readValue(input, RealLocation[].class);
                return Arrays.asList(arr);
            }
        } catch (Exception e) {
            ConsoleLogger.error("Error al buscar ubicaciones: ", e);
            return Collections.emptyList();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
