package com.bteconosur.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;

public class DateUtils {

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

}
