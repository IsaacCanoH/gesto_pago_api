package com.proyecto.servicios.exception;

public class GestoPagoClienteDuplicadoException extends RuntimeException {

    public GestoPagoClienteDuplicadoException(String mensaje) {
        super(mensaje);
    }
}