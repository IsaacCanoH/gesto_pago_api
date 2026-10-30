package com.proyecto.servicios.exception;

public class GestoPagoPagoNoEncontradoException extends RuntimeException {
    public GestoPagoPagoNoEncontradoException() {
        super("No se encontró el pago solicitado");
    }
}
