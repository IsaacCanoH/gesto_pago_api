package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.model.gestopago.GestoPagoFormularioTx;

public interface GestoPagoEnvioPagoService {
    GestoPagoFormularioTx preparar(GestoPagoPago pago);

    GestoPagoRespuestaEnvio enviar(GestoPagoPago pago);

    GestoPagoRespuestaConfirmacion confirmar(GestoPagoPago pago);
}
