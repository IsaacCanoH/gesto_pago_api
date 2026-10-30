package com.proyecto.servicios.service.Impl;

import org.springframework.stereotype.Service;

import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.exception.GestoPagoPagoIndeterminadoException;
import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;
import com.proyecto.servicios.service.GestoPagoEnvioPagoService;
import com.proyecto.servicios.service.GestoPagoEstadoPagoService;
import com.proyecto.servicios.service.GestoPagoPagoWorkflowService;
import com.proyecto.servicios.service.GestoPagoReservaPagoService;
import com.proyecto.servicios.service.GestoPagoPinService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestoPagoPagoWorkflowServiceImpl implements GestoPagoPagoWorkflowService {
    private final GestoPagoReservaPagoService reservaService;
    private final GestoPagoPinService pinService;
    private final GestoPagoEstadoPagoService estadoService;
    private final GestoPagoEnvioPagoService proveedorService;

    @Override
    public GestoPagoPagoConsultaResponse iniciar(GestoPagoReservaPagoCommand command, Integer usuarioId, String pin) {
        pinService.validar(usuarioId, pin);
        var existente = estadoService.buscarSolicitud(command.solicitudId(), command, usuarioId);
        if (existente.isPresent()) {
            log.info("Solicitud de pago repetida UPC={} estado={}",
                    existente.get().upc(), existente.get().estatus());
            return existente.get();
        }
        GestoPagoPago reservado;
        try {
            reservado = reservaService.reservar(command, usuarioId);
        } catch (RuntimeException e) {
            return estadoService.buscarSolicitud(command.solicitudId(), command, usuarioId)
                    .orElseThrow(() -> e);
        }
        GestoPagoPago pago;
        try {
            pago = estadoService.iniciarEnvio(
                    reservado.getId(), proveedorService.preparar(reservado));
        } catch (RuntimeException e) {
            log.error("Pago UPC={} detenido antes de sendTx; tipo={}",
                    reservado.getUpc(), e.getClass().getSimpleName());
            estadoService.cancelarSinEnvio(reservado.getId());
            throw e;
        }
        try {
            estadoService.registrarEnvio(pago.getId(), proveedorService.enviar(pago));
        } catch (GestoPagoPagoIndeterminadoException e) {
            log.warn("Resultado de sendTx incierto para UPC={}; se consultará confirmTx", pago.getUpc());
        }
        return estadoService.consultar(pago.getUpc(), usuarioId);
    }

    @Override
    public void reconciliar(Long pagoId) {
        estadoService.iniciarConfirmacion(pagoId).ifPresent(pago -> {
            try {
                estadoService.registrarConfirmacion(pagoId, proveedorService.confirmar(pago));
            } catch (GestoPagoPagoIndeterminadoException e) {
                log.warn("confirmTx aún no resuelve el pago UPC={}", pago.getUpc());
            }
        });
    }
}
