package com.proyecto.servicios.service;

import java.util.Optional;

import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.model.gestopago.GestoPagoFormularioTx;
import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;

public interface GestoPagoEstadoPagoService {
    GestoPagoPago iniciarEnvio(Long pagoId, GestoPagoFormularioTx formulario);

    void cancelarSinEnvio(Long pagoId);

    void registrarEnvio(Long pagoId, GestoPagoRespuestaEnvio respuesta);

    Optional<GestoPagoPago> iniciarConfirmacion(Long pagoId);

    void registrarConfirmacion(Long pagoId, GestoPagoRespuestaConfirmacion respuesta);

    GestoPagoPagoConsultaResponse consultar(String upc, Integer usuarioId);

}
