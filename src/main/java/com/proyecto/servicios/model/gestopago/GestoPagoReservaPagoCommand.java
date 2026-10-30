package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;

public record GestoPagoReservaPagoCommand(
        String numeroCuenta,
        Long idServicio,
        Long idProducto,
        Integer tipoFront,
        BigDecimal montoPago,
        String telefono,
        String referencia) {

}
