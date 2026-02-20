package com.bteconosur.core.config;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.util.PlaceholderUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageHandler {
    private static final Map<Language, ConfigFile> langByCode = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Inicializa el LanguageHandler cargando todos los archivos de idioma
     */
    public static void initialize() {
        if (initialized) return;
        //ConsoleLogger.info("Cargando archivos de idioma...");
        for (Language language : Language.values()) {
            ConfigFile langFile = new ConfigFile("lang/" + language.getCode() + ".yml");
            langFile.register();
            langByCode.put(language, langFile);
            //ConsoleLogger.info("Cargando archivo de idioma: " + language.getCode());
        }
        initialized = true;
    }

    /**
     * Obtiene el archivo de configuración de idioma
     * @param language Idioma a obtener
     * @return YamlConfiguration del idioma
     */
    private static YamlConfiguration getLanguageConfig(Language language) {
        if (!initialized) initialize();
        return langByCode.getOrDefault(language, langByCode.get(Language.SPANISH)).getFileConfiguration();
    }

    /**
     * Obtiene un texto según el idioma y la clave
     * @param language Idioma
     * @param key Clave a obtener
     * @return Valor de la clave
     */
    public static String getText(Language language, String key) {
        String value = getLanguageConfig(language).getString(key);
        if (value == null) {
            ConsoleLogger.warn(getText("language-key-error").replace("%key%", key).replace("%language%", language.getCode()));
            return "ERROR_KEY_NF";
        }
        return value;
    }

    public static String getTextWithouthWarn(Language language, String key) {
        String value = getLanguageConfig(language).getString(key);
        if (value == null) return "ERROR_KEY_NF";
        return value;
    }

    public static String getText(String key) {
        String value = getLanguageConfig(Language.getDefault()).getString(key);
        if (value == null) {
            ConsoleLogger.warn(getText("language-key-error").replace("%key%", key).replace("%language%", Language.getDefault().getCode()));
            return "ERROR_KEY_NF";
        }
        return value;
    }

    /**
     * Obtiene una lista de textos según el idioma y la clave
     * @param language Idioma
     * @param key Clave a obtener
     * @return Lista de valores
     */
    public static List<String> getTextList(Language language, String key) {
        List<String> list = getLanguageConfig(language).getStringList(key);
        return list;
    }

    public static String replaceMC(String key, Language language, List<Player> players, List<Proyecto> proyectos) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceMC(text, language, players.toArray(new Player[0]));
        return PlaceholderUtils.replaceMC(replaced, language, proyectos.toArray(new Proyecto[0]));
    }

    public static String replaceDS(String key, Language language, List<Player> players, List<Proyecto> proyectos) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceDS(text, language, players.toArray(new Player[0]));
        return PlaceholderUtils.replaceDS(replaced, language, proyectos.toArray(new Proyecto[0]));
    }

    public static String replaceMC(String key, Language language, Player player, Proyecto proyecto) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceMC(text, language, player);
        return PlaceholderUtils.replaceMC(replaced, language, proyecto);
    }

    public static String replaceDS(String key, Language language, Player player, Pais pais) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceDS(text, language, player);
        return PlaceholderUtils.replaceDS(replaced, language, pais);
    }

    public static String replaceMC(String key, Language language, Player player, Pais pais) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceMC(text, language, player);
        return PlaceholderUtils.replaceMC(replaced, language, pais);
    }

    public static String replaceDS(String key, Language language, Player player, Proyecto proyecto) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceDS(text, language, player);
        return PlaceholderUtils.replaceDS(replaced, language, proyecto);
    }

    public static String replaceMC(String key, Language language, Player... players) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, players);
    }

    public static String replaceDS(String key, Language language, Player... players) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, players);
    }

    public static String replaceMC(String key, Language language, Proyecto... proyectos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, proyectos);
    }

    public static String replaceDS(String key, Language language, Proyecto... proyectos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, proyectos);
    }

    public static String replaceMC(String key, Language language, Pais... paises) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, paises);
    }

    public static String replaceDS(String key, Language language, Pais... paises) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, paises);
    }

    public static String replaceMC(String key, Language language, RangoUsuario... rangos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, rangos);
    }

    public static String replaceDS(String key, Language language, RangoUsuario... rangos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, rangos);
    }

    public static String replaceMC(String key, Language language, TipoUsuario... tipos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, tipos);
    }

    public static String replaceDS(String key, Language language, TipoUsuario... tipos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, tipos);
    }

    public static String replaceMC(String key, Language language, Division... divisiones) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, divisiones);
    }

    public static String replaceDS(String key, Language language, Division... divisiones) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, divisiones);
    }

    /**
     * Recarga todos los archivos de idioma
     */
    public static void reload() {
        for (ConfigFile langFile : langByCode.values()) {
            langFile.reload();
        }
    }

    /**
     * Guarda todos los archivos de idioma
     */
    public static void save() {
        for (ConfigFile langFile : langByCode.values()) {
            langFile.save();
        }
    }
}
