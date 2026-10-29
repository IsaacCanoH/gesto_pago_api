package com.proyecto.servicios.exception;

public class GestoPagoPinNoConfiguradoException extends RuntimeException {
    public GestoPagoPinNoConfiguradoException() {
        super("El cliente debe configurar su PIN antes de realizar pagos");
    }
}
