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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Manejador de textos multiidioma con sistema de placeholders.
 */
public class LanguageHandler {
    private static final Map<Language, ConfigFile> langByCode = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Inicializa el manejador de idiomas cargando todos los archivos de traducciones.
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
     * Obtiene la configuración YAML del idioma solicitado.
     *
     * @param language idioma a obtener.
     * @return {@code YamlConfiguration} del idioma o del español por defecto si no existe.
     */
    private static YamlConfiguration getLanguageConfig(Language language) {
        if (!initialized) initialize();
        return langByCode.getOrDefault(language, langByCode.get(Language.SPANISH)).getFileConfiguration();
    }

    /**
     * Obtiene un texto traducido según idioma y clave, emitiendo advertencia si no existe.
     *
     * @param language idioma en el que se busca el texto.
     * @param key clave de la traducción.
     * @return texto traducido o {@code "ERROR_KEY_NF"} si no se encuentra.
     */
    public static String getText(Language language, String key) {
        if (language == null) language = Language.getDefault();
        String value = getLanguageConfig(language).getString(key);
        if (value == null) {
            ConsoleLogger.warn(getText("language-key-error").replace("%key%", key).replace("%language%", language.getCode()));
            return "ERROR_KEY_NF";
        }
        return value;
    }

    /**
     * Obtiene un texto traducido sin emitir advertencia en consola si falta.
     *
     * @param language idioma en el que se busca el texto.
     * @param key clave de la traducción.
     * @return texto traducido o {@code "ERROR_KEY_NF"} si no se encuentra.
     */
    public static String getTextWithouthWarn(Language language, String key) {
        if (language == null) language = Language.getDefault();
        String value = getLanguageConfig(language).getString(key);
        if (value == null) return "ERROR_KEY_NF";
        return value;
    }

    /**
     * Obtiene un texto traducido usando el idioma predeterminado del servidor.
     *
     * @param key clave de la traducción.
     * @return texto traducido o {@code "ERROR_KEY_NF"} si no se encuentra.
     */
    public static String getText(String key) {
        String value = getLanguageConfig(Language.getDefault()).getString(key);
        if (value == null) {
            ConsoleLogger.warn(getText("language-key-error").replace("%key%", key).replace("%language%", Language.getDefault().getCode()));
            return "ERROR_KEY_NF";
        }
        return value;
    }

    /**
     * Obtiene una lista de textos traducidos según idioma y clave.
     *
     * @param language idioma en el que se busca la lista.
     * @param key clave de la lista de traducciones.
     * @return lista de textos traducidos.
     */
    public static List<String> getTextList(Language language, String key) {
        if (language == null) language = Language.getDefault();
        List<String> list = getLanguageConfig(language).getStringList(key);
        return list;
    }

    /**
     * Obtiene un texto según clave e idioma y reemplaza placeholders de jugadores y proyectos para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param players lista de jugadores cuyos placeholders se reemplazan.
     * @param proyectos lista de proyectos cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, List<Player> players, List<Proyecto> proyectos) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceMC(text, language, players.toArray(new Player[0]));
        return PlaceholderUtils.replaceMC(replaced, language, proyectos.toArray(new Proyecto[0]));
    }

    /**
     * Obtiene un texto según clave e idioma y reemplaza placeholders de jugadores y proyectos para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param players lista de jugadores cuyos placeholders se reemplazan.
     * @param proyectos lista de proyectos cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, List<Player> players, List<Proyecto> proyectos) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceDS(text, language, players.toArray(new Player[0]));
        return PlaceholderUtils.replaceDS(replaced, language, proyectos.toArray(new Proyecto[0]));
    }

    /**
     * Obtiene un texto y reemplaza placeholders de un jugador y un proyecto para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param player jugador cuyos placeholders se reemplazan.
     * @param proyecto proyecto cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, Player player, Proyecto proyecto) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceMC(text, language, player);
        return PlaceholderUtils.replaceMC(replaced, language, proyecto);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de un jugador y un país para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param player jugador cuyos placeholders se reemplazan.
     * @param pais país cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, Player player, Pais pais) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceDS(text, language, player);
        return PlaceholderUtils.replaceDS(replaced, language, pais);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de un jugador y un país para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param player jugador cuyos placeholders se reemplazan.
     * @param pais país cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, Player player, Pais pais) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceMC(text, language, player);
        return PlaceholderUtils.replaceMC(replaced, language, pais);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de un jugador y un proyecto para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param player jugador cuyos placeholders se reemplazan.
     * @param proyecto proyecto cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, Player player, Proyecto proyecto) {
        String text = getText(language, key);
        String replaced = PlaceholderUtils.replaceDS(text, language, player);
        return PlaceholderUtils.replaceDS(replaced, language, proyecto);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de jugadores para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param players jugadores cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, Player... players) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, players);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de jugadores para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param players jugadores cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, Player... players) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, players);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de proyectos para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param proyectos proyectos cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, Proyecto... proyectos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, proyectos);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de proyectos para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param proyectos proyectos cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, Proyecto... proyectos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, proyectos);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de países para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param paises países cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, Pais... paises) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, paises);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de países para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param paises países cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, Pais... paises) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, paises);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de rangos de usuario para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param rangos rangos de usuario cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, RangoUsuario... rangos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, rangos);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de rangos de usuario para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param rangos rangos de usuario cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, RangoUsuario... rangos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, rangos);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de tipos de usuario para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param tipos tipos de usuario cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, TipoUsuario... tipos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, tipos);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de tipos de usuario para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param tipos tipos de usuario cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, TipoUsuario... tipos) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, tipos);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de divisiones para Minecraft.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param divisiones divisiones cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Minecraft.
     */
    public static String replaceMC(String key, Language language, Division... divisiones) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceMC(text, language, divisiones);
    }

    /**
     * Obtiene un texto y reemplaza placeholders de divisiones para Discord.
     *
     * @param key clave de la traducción.
     * @param language idioma del texto.
     * @param divisiones divisiones cuyos placeholders se reemplazan.
     * @return texto con placeholders procesados para Discord.
     */
    public static String replaceDS(String key, Language language, Division... divisiones) {
        String text = getText(language, key);
        return PlaceholderUtils.replaceDS(text, language, divisiones);
    }

    /**
     * Detecta el idioma del jugador según su configuración local de Minecraft.
     *
     * @param player jugador de Bukkit cuyo idioma se detecta.
     * @return idioma detectado o {@code null} si no coincide con ninguno soportado.
     */
    public static Language checkDefaultLang(org.bukkit.entity.Player player) {
        Locale locale = player.locale();
        String codigo = locale.getLanguage() + "_" + locale.getCountry();
        Map<Language, Set<String>> relatedCodes = Language.getAllRelatedCodes();
        for (Entry<Language, Set<String>> entry : relatedCodes.entrySet()) {
            if (entry.getValue().contains(codigo)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Recarga todos los archivos de idioma desde disco.
     */
    public static void reload() {
        for (ConfigFile langFile : langByCode.values()) {
            langFile.reload();
        }
    }

    /**
     * Guarda todos los archivos de idioma en disco.
     */
    public static void save() {
        for (ConfigFile langFile : langByCode.values()) {
            langFile.save();
        }
    }
}
