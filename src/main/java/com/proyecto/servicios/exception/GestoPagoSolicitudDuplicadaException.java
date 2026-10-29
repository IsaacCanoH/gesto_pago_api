package com.proyecto.servicios.exception;

public class GestoPagoSolicitudDuplicadaException extends RuntimeException {
    public GestoPagoSolicitudDuplicadaException() {
        super("La clave de solicitud ya se usó para un pago distinto");
    }
}
