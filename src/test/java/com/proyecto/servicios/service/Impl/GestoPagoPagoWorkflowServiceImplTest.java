package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.exception.GestoPagoPinInvalidoException;
import com.proyecto.servicios.model.gestopago.GestoPagoFormularioTx;
import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.service.GestoPagoEnvioPagoService;
import com.proyecto.servicios.service.GestoPagoEstadoPagoService;
import com.proyecto.servicios.service.GestoPagoReservaPagoService;
import com.proyecto.servicios.service.GestoPagoPinService;

@ExtendWith(MockitoExtension.class)
class GestoPagoPagoWorkflowServiceImplTest {
    @Mock private GestoPagoReservaPagoService reservas;
    @Mock private GestoPagoPinService pinService;
    @Mock private GestoPagoEstadoPagoService estados;
    @Mock private GestoPagoEnvioPagoService proveedor;

    @Test
    void respuestaInciertaSeQuedaPendienteParaConfirmacion() {
        GestoPagoPago pago = pago();
        GestoPagoReservaPagoCommand command = new GestoPagoReservaPagoCommand(
                "00000000000000000007", 456L, 205L, 2,
                new BigDecimal("30.00"), "1111111111", "25720381");
        GestoPagoFormularioTx formulario = new GestoPagoFormularioTx("random", "signed");
        GestoPagoPagoConsultaResponse consulta = new GestoPagoPagoConsultaResponse(
                pago.getUpc(), command.numeroCuenta(), 205L, command.montoPago(),
                GestoPagoEstadoPago.PENDIENTE, false, null, null, null, null);
        when(reservas.reservar(command, 4)).thenReturn(pago);
        when(proveedor.preparar(pago)).thenReturn(formulario);
        when(estados.iniciarEnvio(12L, formulario)).thenReturn(pago);
        when(proveedor.enviar(pago)).thenReturn(new GestoPagoRespuestaEnvio("82", "Timeout", null, "-1"));
        when(estados.consultar(pago.getUpc(), 4)).thenReturn(consulta);
        GestoPagoPagoWorkflowServiceImpl workflow =
                new GestoPagoPagoWorkflowServiceImpl(reservas, pinService, estados, proveedor);

        assertEquals(GestoPagoEstadoPago.PENDIENTE, workflow.iniciar(command, 4, "123456").estatus());
        verify(pinService).validar(4, "123456");
        verify(estados).registrarEnvio(12L, new GestoPagoRespuestaEnvio("82", "Timeout", null, "-1"));
        verify(proveedor, never()).confirmar(any());
    }

    @Test
    void pinIncorrectoDetieneElPagoAntesDeLaReserva() {
        GestoPagoReservaPagoCommand command = new GestoPagoReservaPagoCommand(
                "00000000000000000007", 456L, 205L, 1,
                new BigDecimal("30.00"), "5512345678", null);
        doThrow(new GestoPagoPinInvalidoException()).when(pinService).validar(4, "000000");
        GestoPagoPagoWorkflowServiceImpl workflow =
                new GestoPagoPagoWorkflowServiceImpl(reservas, pinService, estados, proveedor);

        assertThrows(GestoPagoPinInvalidoException.class,
                () -> workflow.iniciar(command, 4, "000000"));

        verifyNoInteractions(reservas, estados, proveedor);
    }

    @Test
    void reconciliacionSoloLlamaConfirmTx() {
        GestoPagoPago pago = pago();
        GestoPagoRespuestaConfirmacion confirmacion =
                new GestoPagoRespuestaConfirmacion("70", "No aplicada", "-1", "0");
        when(estados.iniciarConfirmacion(12L)).thenReturn(Optional.of(pago));
        when(proveedor.confirmar(pago)).thenReturn(confirmacion);
        GestoPagoPagoWorkflowServiceImpl workflow =
                new GestoPagoPagoWorkflowServiceImpl(reservas, pinService, estados, proveedor);

        workflow.reconciliar(12L);

        verify(estados).registrarConfirmacion(12L, confirmacion);
        verify(proveedor, never()).enviar(any());
    }

    private GestoPagoPago pago() {
        GestoPagoPago pago = new GestoPagoPago();
        pago.setId(12L);
        pago.setUpc("P12345678901234");
        return pago;
    }
}
