package com.proyecto.servicios.exception;

public class GestoPagoPinInvalidoException extends RuntimeException {
    public GestoPagoPinInvalidoException() {
        super("PIN de autorización inválido");
    }
}
