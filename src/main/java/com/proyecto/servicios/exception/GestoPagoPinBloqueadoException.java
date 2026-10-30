package com.proyecto.servicios.exception;

public class GestoPagoPinBloqueadoException extends RuntimeException {
    public GestoPagoPinBloqueadoException() {
        super("PIN bloqueado temporalmente por intentos fallidos");
    }
}
