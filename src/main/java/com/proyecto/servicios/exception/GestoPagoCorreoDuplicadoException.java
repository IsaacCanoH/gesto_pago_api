package com.proyecto.servicios.exception;

public class GestoPagoCorreoDuplicadoException extends RuntimeException {
    public GestoPagoCorreoDuplicadoException() {
        super("El correo ya está registrado");
    }
}
