package com.proyecto.servicios.exception;

public class GestoPagoCuentaNoEncontradaException extends RuntimeException {

    public GestoPagoCuentaNoEncontradaException() {
        super("Cuenta no encontrada");
    }
}