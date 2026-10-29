package com.proyecto.servicios.service;

import java.util.Optional;
import java.util.UUID;

import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.model.gestopago.GestoPagoFormularioTx;
import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;

public interface GestoPagoEstadoPagoService {
    GestoPagoPago iniciarEnvio(Long pagoId, GestoPagoFormularioTx formulario);

    void cancelarSinEnvio(Long pagoId);

    void registrarEnvio(Long pagoId, GestoPagoRespuestaEnvio respuesta);

    Optional<GestoPagoPago> iniciarConfirmacion(Long pagoId);

    void registrarConfirmacion(Long pagoId, GestoPagoRespuestaConfirmacion respuesta);

    GestoPagoPagoConsultaResponse consultar(String upc, Integer usuarioId);

    Optional<GestoPagoPagoConsultaResponse> buscarSolicitud(
            UUID solicitudId, GestoPagoReservaPagoCommand command, Integer usuarioId);
}
