package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;

public interface GestoPagoPagoWorkflowService {
    GestoPagoPagoConsultaResponse iniciar(GestoPagoReservaPagoCommand command, Integer usuarioId, String pin);

    void reconciliar(Long pagoId);
}
