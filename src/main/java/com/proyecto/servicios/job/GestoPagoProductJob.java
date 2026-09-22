package com.proyecto.servicios.job;

import com.proyecto.servicios.service.GestoPagoProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GestoPagoProductJob {

    private final GestoPagoProductService gestoPagoProductService;

    public GestoPagoProductJob(
            GestoPagoProductService gestoPagoProductService) {
        this.gestoPagoProductService = gestoPagoProductService;
    }

    @Scheduled(cron = "${gestopago.product.sync-cron:0 0 12 * * *}", zone = "${gestopago.product.time-zone:America/Mexico_City}")
    public void sincronizarProductos() {
        try {
            int productosSincronizados = gestoPagoProductService.sincronizarProductos();

            log.info("Job GestoPago terminado. Productos sincronizados: {}",
                    productosSincronizados);
        } catch (Exception e) {
            log.error(
                    "Error al sincronizar productos de GestoPago. Tipo: {}",
                    e.getClass().getSimpleName());
        }
    }
}