package com.proyecto.servicios.exception;

public class GestoPagoUsuarioNoEncontradoException extends RuntimeException {
    public GestoPagoUsuarioNoEncontradoException() {
        super("Usuario no encontrado");
    }
}
