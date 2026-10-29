package com.proyecto.servicios.exception;

public class GestoPagoPagoInvalidoException extends RuntimeException {
    public GestoPagoPagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
