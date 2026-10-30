package com.proyecto.servicios.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gestopago.auth")
public record GestoPagoIdentidadProveedorProperties(
        @NotNull Integer idDistribuidor,
        @NotBlank String codigoDispositivo) {
}
