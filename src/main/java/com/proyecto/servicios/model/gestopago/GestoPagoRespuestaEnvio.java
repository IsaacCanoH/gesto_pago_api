package com.proyecto.servicios.model.gestopago;

public record GestoPagoRespuestaEnvio(
        String codigo,
        String mensaje,
        String idTransaccion,
        String numeroAutorizacion) {

    public boolean exitosa() {
        return "01".equals(codigo)
                && idTransaccion != null && !idTransaccion.isBlank() && !"-1".equals(idTransaccion)
                && numeroAutorizacion != null && !numeroAutorizacion.isBlank()
                && !"0".equals(numeroAutorizacion) && !"-1".equals(numeroAutorizacion);
    }

    public boolean requiereConfirmacion() {
        return !exitosa() && !rechazada();
    }

    public boolean rechazada() {
        return "02".equals(codigo);
    }
}
