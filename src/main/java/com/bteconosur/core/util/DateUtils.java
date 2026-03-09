package com.bteconosur.core.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;

/**
 * Utilidad para formateo y conversión de fechas y marcas de tiempo.
 * Proporciona métodos para convertir instantes a diferentes formatos de fecha
 * localizados y marcas de tiempo de Discord.
 */
public class DateUtils {

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    /**
     * Formatea una fecha con hora en formato legible según el idioma.
     *
     * @param date fecha a formatear.
     * @param language idioma para localización.
     * @return fecha formateada como {@code "dd de MMMM de yyyy a las HH:mm"} o placeholder correspondiente si es null.
     */
    public static String formatDateHour(Date date, Language language) {
        if (date == null) return LanguageHandler.getText(language, "placeholder.without-fecha");
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.forLanguageTag("es-ES"));
        return sdf.format(date);
    }

    /**
     * Formatea una fecha sin hora en formato corto.
     *
     * @param date fecha a formatear.
     * @param language idioma para localización.
     * @return fecha formateada como {@code "dd/MM/yyyy"} o placeholder correspondiente si es null.
     */
    public static String formatDate(Date date, Language language) {
        if (date == null) return LanguageHandler.getText(language, "placeholder.without-fecha");
        SimpleDateFormat sdf = new SimpleDateFormat("dd'/'MM'/'yyyy", Locale.forLanguageTag("es-ES"));
        return sdf.format(date);
    }

    /**
     * Obtiene la timestamp de Discord para una fecha.
     * Convierte la fecha a timestamp para usar en mensajes embebidos de Discord.
     *
     * @param date fecha a convertir.
     * @param language idioma para placeholder si es null.
     * @return timestamp Discord como {@code "<t:unix:F>"} o placeholder correspondiente si es null.
     */
    public static String getDsTimestamp(Date date, Language language) {
        if (date == null) return LanguageHandler.getText(language, "placeholder.without-fecha");
        long unixTimestamp = date.getTime() / 1000;
        return "<t:" + unixTimestamp + ":F>";
    }

    /**
     * Obtiene el instante actual más el offset de zona horaria configurada.
     * Debería usar en ves de {@code Instant.now()}.
     *
     * @return instante con offset de GMT aplicado.
     */
    public static Instant instantOffset() {
        Instant now = Instant.now();
        return now.plus(config.getInt("gmt-offset"), ChronoUnit.HOURS);
    }

}
