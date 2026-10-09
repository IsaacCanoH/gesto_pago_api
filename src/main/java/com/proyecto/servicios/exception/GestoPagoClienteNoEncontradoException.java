package com.proyecto.servicios.exception;

public class GestoPagoClienteNoEncontradoException extends RuntimeException {

    public GestoPagoClienteNoEncontradoException() {
        super("Cliente no encontrado");
    }
}