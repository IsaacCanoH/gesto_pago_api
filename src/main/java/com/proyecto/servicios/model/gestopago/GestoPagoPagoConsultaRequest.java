package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GestoPagoPagoConsultaRequest(
        @NotBlank @Size(min = 3, max = 15) String upc) {
}
