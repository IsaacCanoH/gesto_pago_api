package com.proyecto.servicios.exception;

public class GestoPagoUsuarioInactivoException extends RuntimeException {
    public GestoPagoUsuarioInactivoException() {
        super("El usuario está inactivo");
    }
}
