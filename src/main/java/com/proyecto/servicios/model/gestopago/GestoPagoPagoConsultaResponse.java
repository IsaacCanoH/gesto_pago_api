package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Duration;

import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;

public record GestoPagoPagoConsultaResponse(
        String upc,
        String numeroCuenta,
        Long idProducto,
        BigDecimal monto,
        GestoPagoEstadoPago estatus,
        boolean requiereRevisionManual,
        Instant fechaCreacion,
        Instant fechaResolucion,
        String numeroAutorizacion,
        String codigoProveedor) {

    public static GestoPagoPagoConsultaResponse desde(GestoPagoPago pago) {
        return new GestoPagoPagoConsultaResponse(
                pago.getUpc(),
                pago.getCuenta().getNumeroCuenta(),
                pago.getIdProductoProveedor(),
                pago.getMontoPago(),
                pago.getEstatus(),
                pago.getEstatus() == GestoPagoEstadoPago.PENDIENTE
                        && pago.getFechaEnvio() != null
                        && pago.getFechaEnvio().isBefore(Instant.now().minus(Duration.ofHours(24))),
                pago.getFechaCreacion(),
                pago.getFechaResolucion(),
                pago.getEstatus() == GestoPagoEstadoPago.EXITOSO
                        ? pago.getNumeroAutorizacion() : null,
                pago.getCodigoProveedor());
    }
}
