package com.proyecto.servicios.exception;

public class GestoPagoAuthenticationException extends RuntimeException {

    public GestoPagoAuthenticationException(String message) {
        super(message);
    }

    public GestoPagoAuthenticationException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}