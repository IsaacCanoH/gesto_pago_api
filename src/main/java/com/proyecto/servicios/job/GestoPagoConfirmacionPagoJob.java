package com.proyecto.servicios.job;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.proyecto.servicios.repositorys.gestopago.GestoPagoPagoRepository;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.service.GestoPagoEstadoPagoService;
import com.proyecto.servicios.service.GestoPagoPagoWorkflowService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GestoPagoConfirmacionPagoJob {
    private final GestoPagoPagoRepository pagoRepository;
    private final GestoPagoPagoWorkflowService workflowService;
    private final GestoPagoEstadoPagoService estadoService;

    @Scheduled(fixedDelayString = "${gestopago.pago.confirm-interval-ms:30000}",
            initialDelayString = "${gestopago.pago.confirm-initial-delay-ms:30000}")
    public void confirmarPendientes() {
        Instant ahora = Instant.now();
        pagoRepository.buscarPendientesDeConfirmacion(
                GestoPagoEstadoPago.PENDIENTE,
                ahora.minus(Duration.ofHours(24)),
                ahora.minus(Duration.ofSeconds(90)),
                ahora.minus(Duration.ofMinutes(1)),
                PageRequest.of(0, 50))
                .forEach(id -> {
                    try {
                        workflowService.reconciliar(id);
                    } catch (RuntimeException e) {
                        log.warn("No se pudo confirmar el pago id={}; se intentará después. Tipo={}",
                                id, e.getClass().getSimpleName());
                    }
                });
        pagoRepository.buscarReservasSinEnvio(
                GestoPagoEstadoPago.PENDIENTE,
                ahora.minus(Duration.ofMinutes(5)),
                PageRequest.of(0, 50))
                .forEach(id -> {
                    try {
                        estadoService.cancelarSinEnvio(id);
                    } catch (RuntimeException e) {
                        log.warn("No se pudo liberar la reserva sin envío id={}; tipo={}",
                                id, e.getClass().getSimpleName());
                    }
                });
    }

    @Scheduled(fixedDelayString = "${gestopago.pago.manual-review-interval-ms:3600000}",
            initialDelayString = "${gestopago.pago.manual-review-initial-delay-ms:3600000}")
    public void alertarRevisionManual() {
        long pendientes = pagoRepository.countByEstatusAndFechaEnvioBefore(
                GestoPagoEstadoPago.PENDIENTE, Instant.now().minus(Duration.ofHours(24)));
        if (pendientes > 0) {
            log.warn("Hay {} pagos pendientes fuera de la vigencia de confirmTx; requieren conciliación manual",
                    pendientes);
        }
    }
}
