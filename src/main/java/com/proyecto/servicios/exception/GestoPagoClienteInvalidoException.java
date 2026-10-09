package com.proyecto.servicios.exception;

public class GestoPagoClienteInvalidoException extends RuntimeException {

    public GestoPagoClienteInvalidoException(String mensaje) {
        super(mensaje);
    }
}