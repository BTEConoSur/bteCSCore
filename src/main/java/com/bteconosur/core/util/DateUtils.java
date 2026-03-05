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

public class DateUtils {

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static String formatDateHour(Date date, Language language) {
        if (date == null) return LanguageHandler.getText(language, "placeholder.without-fecha");
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.forLanguageTag("es-ES"));
        return sdf.format(date);
    }

    public static String formatDate(Date date, Language language) {
        if (date == null) return LanguageHandler.getText(language, "placeholder.without-fecha");
        SimpleDateFormat sdf = new SimpleDateFormat("dd'/'MM'/'yyyy", Locale.forLanguageTag("es-ES"));
        return sdf.format(date);
    }

    public static String getDsTimestamp(Date date, Language language) {
        if (date == null) return LanguageHandler.getText(language, "placeholder.without-fecha");
        long unixTimestamp = date.getTime() / 1000;
        return "<t:" + unixTimestamp + ":F>";
    }

    public static Instant instantOffset() {
        Instant now = Instant.now();
        return now.plus(config.getInt("gmt-offset"), ChronoUnit.HOURS);
    }

}
