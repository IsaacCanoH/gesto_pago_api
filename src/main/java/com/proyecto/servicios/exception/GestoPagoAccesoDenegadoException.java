package com.proyecto.servicios.exception;

public class GestoPagoAccesoDenegadoException extends RuntimeException {
    public GestoPagoAccesoDenegadoException() {
        super("No puedes acceder a otro usuario");
    }

    public GestoPagoAccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
