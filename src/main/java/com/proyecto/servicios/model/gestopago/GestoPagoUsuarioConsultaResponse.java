package com.proyecto.servicios.model.gestopago;

import java.time.LocalDate;

public record GestoPagoUsuarioConsultaResponse(
        Integer id,
        Integer clienteId,
        String correo,
        boolean activo,
        LocalDate fechaCreacion,
        LocalDate fechaActualizacion) {
}
