package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GestoPagoCambioContrasenaRequest(
        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor que cero") Integer id,
        @NotBlank(message = "es obligatoria") String contrasenaActual,
        @NotBlank(message = "es obligatoria") String contrasenaNueva) {
}
