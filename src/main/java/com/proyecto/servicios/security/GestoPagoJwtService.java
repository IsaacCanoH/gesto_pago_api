package com.proyecto.servicios.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginResponse;

@Service
public class GestoPagoJwtService {
    private static final String ISSUER = "gestopago-api";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final Duration vigencia;

    public GestoPagoJwtService(
            @Value("${gestopago.jwt.secret}") String secreto,
            @Value("${gestopago.jwt.expiration-minutes:60}") long minutosVigencia) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "GESTOPAGO_JWT_SECRET debe contener al menos 32 bytes y configurarse fuera del código");
        }
        if (minutosVigencia < 1 || minutosVigencia > 1440) {
            throw new IllegalStateException("La vigencia JWT debe estar entre 1 y 1440 minutos");
        }

        algorithm = Algorithm.HMAC256(secreto);
        verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
        vigencia = Duration.ofMinutes(minutosVigencia);
    }

    public GestoPagoLoginResponse generar(Integer usuarioId) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(vigencia);
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuarioId.toString())
                .withIssuedAt(Date.from(ahora))
                .withExpiresAt(Date.from(expiracion))
                .sign(algorithm);

        return new GestoPagoLoginResponse(token, expiracion);
    }

    public Integer verificar(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            if (jwt.getExpiresAt() == null || jwt.getSubject() == null) {
                throw new GestoPagoCredencialesInvalidasException();
            }
            return Integer.valueOf(jwt.getSubject());
        } catch (JWTVerificationException | NumberFormatException exception) {
            throw new GestoPagoCredencialesInvalidasException();
        }
    }
}
