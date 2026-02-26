package com.bteconosur.core.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum Language {
    ENGLISH("en_US"),
    SPANISH("es_ES");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    private Set<String> getRelatedCodes() {
        return ConfigHandler.getInstance().getConfig().getStringList("related-languages." + code).stream().collect(Collectors.toSet());
    }

    public static Map<Language, Set<String>> getAllRelatedCodes() {
        Map<Language, Set<String>> map = new HashMap<>();
        for (Language language : Language.values()) {
            map.put(language, language.getRelatedCodes());
        }
        return map;
    }

    public static Language getDefault() {
        return Language.valueOf(ConfigHandler.getInstance().getConfig().getString("default-language").toUpperCase());
    }

    public static Language getInternationalDefault() {
        return Language.valueOf(ConfigHandler.getInstance().getConfig().getString("international-default-language").toUpperCase());
    }
}
