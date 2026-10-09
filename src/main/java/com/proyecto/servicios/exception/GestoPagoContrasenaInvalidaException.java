package com.proyecto.servicios.exception;

public class GestoPagoContrasenaInvalidaException extends RuntimeException {
    public GestoPagoContrasenaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
