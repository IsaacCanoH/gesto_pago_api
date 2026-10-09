package com.proyecto.servicios.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;

class GestoPagoJwtServiceTest {
    private static final String SECRETO = "clave-de-prueba-con-mas-de-32-bytes-seguros";

    @Test
    void verificaUnTokenEmitido() {
        GestoPagoJwtService service = new GestoPagoJwtService(SECRETO, 60);

        var respuesta = service.generar(7);
        assertEquals(7, service.verificar(respuesta.token()));
    }

    @Test
    void rechazaFirmaIncorrecta() {
        String token = new GestoPagoJwtService(SECRETO, 60).generar(7).token();
        GestoPagoJwtService otroService = new GestoPagoJwtService("otra-clave-de-prueba-con-32-bytes-o-mas", 60);

        assertThrows(GestoPagoCredencialesInvalidasException.class,
                () -> otroService.verificar(token));
    }

    @Test
    void rechazaTokenExpirado() {
        String token = JWT.create()
                .withIssuer("gestopago-api")
                .withSubject("7")
                .withExpiresAt(Date.from(Instant.now().minusSeconds(60)))
                .sign(Algorithm.HMAC256(SECRETO));

        assertThrows(GestoPagoCredencialesInvalidasException.class,
                () -> new GestoPagoJwtService(SECRETO, 60).verificar(token));
    }

    @Test
    void exigeSecretoSuficientementeLargo() {
        assertThrows(IllegalStateException.class,
                () -> new GestoPagoJwtService("corta", 60));
    }
}
