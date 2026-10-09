package com.proyecto.servicios.exception;

public class GestoPagoCredencialesInvalidasException extends RuntimeException {
    public GestoPagoCredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}
