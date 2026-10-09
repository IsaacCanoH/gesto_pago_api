package com.proyecto.servicios.model.gestopago;

import java.time.Instant;

public record GestoPagoLoginResponse(String token, Instant expiraEn) {
}
