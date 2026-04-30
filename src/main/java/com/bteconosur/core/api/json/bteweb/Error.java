package com.bteconosur.core.api.json.bteweb;

import java.util.List;

/**
 * Representa una respuesta de error recibida de la API web de BTE.
 * Contiene información sobre el estado del error, código HTTP, mensaje y detalles específicos.
 */
public class Error {

    private boolean error;
    private List<ErrorDetail> errors;
    private String message;
    private int code;

    public boolean isError() {
        return error;
    }
    public void setError(boolean error) {
        this.error = error;
    }
    public List<ErrorDetail> getErrors() {
        return errors;
    }
    public void setErrors(List<ErrorDetail> errors) {
        this.errors = errors;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
}
