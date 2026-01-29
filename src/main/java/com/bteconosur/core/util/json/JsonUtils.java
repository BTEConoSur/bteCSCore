package com.bteconosur.core.util.json;

import com.bteconosur.core.util.ConsoleLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static String toJson(Object obj) {
        String json = "";

        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            json = json.replace("\r", "");
        } catch (Exception e) {
            ConsoleLogger.error("Error al serializar objeto a JSON: " + e.getMessage());
        }

        return json;
    }
}
