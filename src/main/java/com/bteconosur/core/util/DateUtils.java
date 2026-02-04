package com.bteconosur.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String formatDateHour(Date date) {
        if (date == null) return "Sin Fecha";
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.forLanguageTag("es-ES"));
        return sdf.format(date);
    }

    public static String formatDate(Date date) {
        if (date == null) return "Sin Fecha";
        SimpleDateFormat sdf = new SimpleDateFormat("dd'/'MM'/'yyyy", Locale.forLanguageTag("es-ES"));
        return sdf.format(date);
    }

    public static String getDsTimestamp(Date date) {
        if (date == null) return "Sin Fecha";
        long unixTimestamp = date.getTime() / 1000;
        return "<t:" + unixTimestamp + ":F>";
    }

}
