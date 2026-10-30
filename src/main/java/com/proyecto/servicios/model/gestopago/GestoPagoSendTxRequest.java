package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GestoPagoSendTxRequest(
        Long idProducto,
        Long idServicio,
        String telefono,
        String horaLocal,
        String referencia,
        BigDecimal montoPago,
        String upc,
        String unidad) {

    public static GestoPagoSendTxRequest desde(GestoPagoPago pago) {
        return new GestoPagoSendTxRequest(
                pago.getIdProductoProveedor(), pago.getIdServicioProveedor(),
                pago.getTelefono(), pago.getHoraLocal(), pago.getReferencia(),
                pago.getReferencia() == null ? null : pago.getMontoPago(),
                pago.getUpc(), pago.getUnidad());
    }
}
