package com.bteconosur.db.util;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.util.PlaceholderUtils.PlaceholderContext;

/**
 * Estados posibles del ciclo de vida de un proyecto.
 */
public enum Estado {
    EN_FINALIZACION,
    EN_FINALIZACION_EDICION,
    COMPLETADO,
    EDITANDO,
    REDEFINIENDO,
    ABANDONADO,
    ACTIVO,
    EN_CREACION;

    /**
     * Obtiene el nombre localizado del estado según el contexto de salida.
     *
     * @param context contexto del placeholder ({@code MINECRAFT} o {@code DISCORD}).
     * @param language idioma a utilizar.
     * @return texto localizado del estado.
     */
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

    /**
     * Obtiene el nombre localizado de un estado mediante método estático auxiliar.
     *
     * @param estado estado a traducir.
     * @param context contexto del placeholder.
     * @param language idioma a utilizar.
     * @return texto localizado del estado.
     */
    public static String getDisplayName(Estado estado, PlaceholderContext context, Language language) {
        return estado.getDisplayName(context, language);
    }
}
