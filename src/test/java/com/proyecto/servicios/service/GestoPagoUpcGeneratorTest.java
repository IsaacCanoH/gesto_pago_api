package com.proyecto.servicios.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GestoPagoUpcGeneratorTest {
    private final GestoPagoUpcGenerator generator = new GestoPagoUpcGenerator();

    @Test
    void generaUpcAlfanumericoDentroDelLimiteDelProveedor() {
        for (int i = 0; i < 100; i++) {
            assertTrue(generator.generar().matches("P[A-Z0-9]{14}"));
        }
    }
}
