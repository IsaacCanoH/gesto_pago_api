package com.proyecto.servicios.model.gestopago;

import java.time.LocalDate;

public record GestoPagoClienteResumenResponse(
        Integer id,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String correo,
        boolean activa,
        LocalDate fechaCreacion) {
}
