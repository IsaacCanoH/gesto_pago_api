package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.NotBlank;

public record GestoPagoCambioContrasenaRequest(
        @NotBlank(message = "es obligatoria") String contrasenaActual,
        @NotBlank(message = "es obligatoria") String contrasenaNueva) {
}
