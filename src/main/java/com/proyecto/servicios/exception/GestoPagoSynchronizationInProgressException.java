package com.proyecto.servicios.exception;

public class GestoPagoSynchronizationInProgressException
        extends RuntimeException {

    public GestoPagoSynchronizationInProgressException(String message) {
        super(message);
    }
}