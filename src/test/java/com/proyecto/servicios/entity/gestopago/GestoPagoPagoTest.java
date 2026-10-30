package com.proyecto.servicios.entity.gestopago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class GestoPagoPagoTest {
    @Test
    void iniciaPendienteYRegistraFechas() {
        var pago = new GestoPagoPago();

        pago.alCrear();

        assertEquals(GestoPagoEstadoPago.PENDIENTE, pago.getEstatus());
        assertNotNull(pago.getFechaCreacion());
        assertEquals(pago.getFechaCreacion(), pago.getFechaActualizacion());
    }

    @Test
    void alActualizarConservaLaFechaDeCreacion() {
        var pago = new GestoPagoPago();
        pago.alCrear();
        Instant fechaCreacion = pago.getFechaCreacion();

        pago.alActualizar();

        assertEquals(fechaCreacion, pago.getFechaCreacion());
        assertFalse(pago.getFechaActualizacion().isBefore(fechaCreacion));
    }
}
