package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;

public interface GestoPagoReservaPagoService {
    GestoPagoPago reservar(GestoPagoReservaPagoCommand command, Integer usuarioId);
}
