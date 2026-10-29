package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;
import java.util.UUID;

public record GestoPagoReservaPagoCommand(
        String numeroCuenta,
        Long idServicio,
        Long idProducto,
        Integer tipoFront,
        BigDecimal montoPago,
        String telefono,
        String referencia,
        UUID solicitudId) {

}
