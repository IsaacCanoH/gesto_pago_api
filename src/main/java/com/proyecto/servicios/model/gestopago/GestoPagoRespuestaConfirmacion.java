package com.proyecto.servicios.model.gestopago;

public record GestoPagoRespuestaConfirmacion(
        String codigo,
        String mensaje,
        String idTransaccion,
        String numeroAutorizacion) {

    public boolean aplicada() {
        return "06".equals(codigo)
                && idTransaccion != null && !idTransaccion.isBlank() && !"-1".equals(idTransaccion)
                && numeroAutorizacion != null && !numeroAutorizacion.isBlank()
                && !"0".equals(numeroAutorizacion) && !"-1".equals(numeroAutorizacion);
    }

    public boolean noAplicada() {
        return "70".equals(codigo);
    }
}
