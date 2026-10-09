package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GestoPagoLoginRequest(
        @NotBlank(message = "es obligatorio")
        @Email(message = "debe tener un formato válido") String correo,
        @NotBlank(message = "es obligatoria") String contrasena) {
    public GestoPagoLoginRequest {
        correo = TextoRecortable.trim(correo);
    }
}
