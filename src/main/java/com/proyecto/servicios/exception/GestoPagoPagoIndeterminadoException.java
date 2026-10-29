package com.proyecto.servicios.exception;

public class GestoPagoPagoIndeterminadoException extends RuntimeException {
    public GestoPagoPagoIndeterminadoException(Throwable cause) {
        super("No se pudo confirmar el resultado del pago; es necesario consultarlo antes de liberar la retención", cause);
    }
}
