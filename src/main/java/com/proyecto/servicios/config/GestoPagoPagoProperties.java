package com.proyecto.servicios.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "gestopago.pago")
public record GestoPagoPagoProperties(
        @NotBlank @Size(min = 3, max = 25) String unidad) {
}
