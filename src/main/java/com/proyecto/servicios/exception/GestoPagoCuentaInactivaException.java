package com.proyecto.servicios.exception;

public class GestoPagoCuentaInactivaException extends RuntimeException {
    public GestoPagoCuentaInactivaException() {
        super("La cuenta está inactiva");
    }
}
