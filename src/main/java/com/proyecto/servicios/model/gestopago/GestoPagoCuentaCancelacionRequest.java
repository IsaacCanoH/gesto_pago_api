package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GestoPagoCuentaCancelacionRequest(
        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = "^[0-9]{20}$", message = "debe tener exactamente 20 dígitos")
        String numeroCuenta) {
}
