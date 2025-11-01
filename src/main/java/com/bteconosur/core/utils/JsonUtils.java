package com.bteconosur.core.utils;

import com.bteconosur.core.BTEConoSur;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static ConsoleLogger consoleLogger = BTEConoSur.getConsoleLogger();
    
    public static String toJson(Object obj) {
        String json = "";

        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            consoleLogger.error("Error al serializar objeto a JSON: " + e.getMessage());
        }

        return json;
    }
}
