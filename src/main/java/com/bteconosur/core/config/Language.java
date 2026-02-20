package com.bteconosur.core.config;

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

    public static Language getDefault() {
        return Language.valueOf(ConfigHandler.getInstance().getConfig().getString("default-language").toUpperCase());
    }
}
