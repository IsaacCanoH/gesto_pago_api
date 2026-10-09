package com.proyecto.servicios.exception;

public class GestoPagoCuentaNoCancelableException extends RuntimeException {
    public GestoPagoCuentaNoCancelableException() {
        super("La cuenta debe tener saldo cero para cancelarse");
    }
}
