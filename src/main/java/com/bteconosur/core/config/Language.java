package com.bteconosur.core.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Idiomas soportados por el plugin con sus códigos de localización.
 */
public enum Language {
    ENGLISH("en_US"),
    SPANISH("es_ES");

    private final String code;

    /**
     * Constructor privado del enum.
     *
     * @param code código ISO del idioma (formato {@code lang_COUNTRY}).
     */
    Language(String code) {
        this.code = code;
    }

    /**
     * Obtiene el idioma correspondiente a un código.
     *
     * @param code código ISO del idioma.
     * @return idioma correspondiente o {@code null} si no se encuentra.
     */
    public static Language getByCode(String code) {
        for (Language language : Language.values()) {
            if (language.getCode().equalsIgnoreCase(code)) return language;
        }
        return null;
    }

    /**
     * Obtiene el código ISO de este idioma.
     *
     * @return código del idioma.
     */
    public String getCode() {
        return code;
    }

    /**
     * Obtiene los códigos de localización relacionados desde la configuración.
     *
     * @return conjunto de códigos relacionados al idioma.
     */
    private Set<String> getRelatedCodes() {
        return ConfigHandler.getInstance().getConfig().getStringList("related-languages." + code).stream().collect(Collectors.toSet());
    }

    /**
     * Obtiene un mapa de todos los idiomas con sus códigos relacionados.
     *
     * @return mapa de idioma a conjunto de códigos relacionados.
     */
    public static Map<Language, Set<String>> getAllRelatedCodes() {
        Map<Language, Set<String>> map = new HashMap<>();
        for (Language language : Language.values()) {
            map.put(language, language.getRelatedCodes());
        }
        return map;
    }

    /**
     * Obtiene el idioma predeterminado del servidor según configuración.
     *
     * @return idioma predeterminado del servidor.
     */
    public static Language getDefault() {
        return Language.getByCode(ConfigHandler.getInstance().getConfig().getString("default-language").toUpperCase());
    }

    /**
     * Obtiene el idioma internacional predeterminado según configuración.
     *
     * @return idioma internacional predeterminado.
     */
    public static Language getInternationalDefault() {
        return Language.getByCode(ConfigHandler.getInstance().getConfig().getString("international-default-language").toUpperCase());
    }
}
