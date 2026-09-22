package com.proyecto.servicios.exception;

public class GestoPagoExternalResponseException extends RuntimeException {

    public GestoPagoExternalResponseException(String message) {
        super(message);
    }

    public GestoPagoExternalResponseException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}