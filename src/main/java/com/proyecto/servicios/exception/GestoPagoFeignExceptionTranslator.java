package com.proyecto.servicios.exception;

import feign.FeignException;
import feign.RetryableException;

public final class GestoPagoFeignExceptionTranslator {

    private GestoPagoFeignExceptionTranslator() {
    }

    public static RuntimeException traducir(FeignException exception) {
        if (exception instanceof RetryableException) {
            return new GestoPagoCommunicationException(
                    "No fue posible comunicarse con GestoPago",
                    exception
            );
        }

        if (exception.status() == 401 || exception.status() == 403) {
            return new GestoPagoAuthenticationException(
                    "GestoPago rechazó la autenticación",
                    exception
            );
        }

        return new GestoPagoExternalResponseException(
                "GestoPago respondió con error HTTP "
                        + exception.status(),
                exception
        );
    }
}