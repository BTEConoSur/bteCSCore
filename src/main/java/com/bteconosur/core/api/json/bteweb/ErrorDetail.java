package com.bteconosur.core.api.json.bteweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representa el detalle específico de un error individual en una respuesta de error de la API web.
 * Contiene el mensaje de error y la ruta del campo que causó el error.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDetail {

    private String msg;
    private String path;

    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

}
