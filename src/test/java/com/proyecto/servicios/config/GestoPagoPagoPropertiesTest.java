package com.proyecto.servicios.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

class GestoPagoPagoPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(Configuracion.class);

    @Test
    void cargaUnidadConfigurada() {
        contextRunner.withPropertyValues(
                        "gestopago.pago.unidad=083",
                        "gestopago.auth.id-distribuidor=83",
                        "gestopago.auth.codigo-dispositivo=GPS83-TPV-17")
                .run(contexto -> assertEquals("083",
                        contexto.getBean(GestoPagoPagoProperties.class).unidad()));
    }

    @Test
    void permiteIniciarSinLlavesDePagoHastaQueSeUseSendTx() {
        contextRunner.withPropertyValues(
                        "gestopago.pago.unidad=083",
                        "gestopago.auth.id-distribuidor=83",
                        "gestopago.auth.codigo-dispositivo=GPS83-TPV-17")
                .run(contexto -> {
                    assertEquals("", contexto.getBean(GestoPagoCifradoProperties.class).getSecretKey());
                    assertEquals(83,
                            contexto.getBean(GestoPagoIdentidadProveedorProperties.class).idDistribuidor());
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({GestoPagoPagoProperties.class,
            GestoPagoCifradoProperties.class, GestoPagoIdentidadProveedorProperties.class})
    static class Configuracion {
    }
}
