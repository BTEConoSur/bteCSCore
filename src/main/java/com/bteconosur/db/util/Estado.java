package com.bteconosur.db.util;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.util.PlaceholderUtils.PlaceholderContext;

public enum Estado {
    EN_FINALIZACION,
    EN_FINALIZACION_EDICION,
    COMPLETADO,
    EDITANDO,
    REDEFINIENDO,
    ABANDONADO,
    ACTIVO,
    EN_CREACION;

    public String getDisplayName(PlaceholderContext context, Language language) {
        String path;
        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.proyecto-mc.estado.";
        else path = "placeholder.proyecto-ds.estado.";
        switch (this) {
            case ACTIVO:
                return LanguageHandler.getText(language, path + "activo");
            case EN_FINALIZACION:
                return LanguageHandler.getText(language, path + "en-finalizacion");
            case EN_FINALIZACION_EDICION:
                return LanguageHandler.getText(language, path + "en-finalizacion-edit");
            case COMPLETADO:
                return LanguageHandler.getText(language, path + "completado");
            case EN_CREACION:
                return LanguageHandler.getText(language, path + "en-creacion");
            case REDEFINIENDO:
                return LanguageHandler.getText(language, path + "redefiniendo");
            case ABANDONADO:
                return LanguageHandler.getText(language, path + "abandonado");
            case EDITANDO:
                return LanguageHandler.getText(language, path + "editando");
            default:
                return "ERROR_MAL_ESTADO";
        }
    }

    public static String getDisplayName(Estado estado, PlaceholderContext context, Language language) {
        return estado.getDisplayName(context, language);
    }
}
