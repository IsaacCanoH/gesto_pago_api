package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoPagoNoEncontradoException;
import com.proyecto.servicios.model.gestopago.GestoPagoFormularioTx;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoPagoRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GestoPagoEstadoPagoServiceImplTest {
    @Mock private GestoPagoPagoRepository pagos;
    @Mock private GestoPagoCuentaRepository cuentas;
    @Mock private GestoPagoUsuarioRepository usuarios;

    private GestoPagoEstadoPagoServiceImpl servicio;
    private GestoPagoPago pago;
    private GestoPagoCuenta cuenta;

    @BeforeEach
    void preparar() {
        servicio = new GestoPagoEstadoPagoServiceImpl(pagos, cuentas, usuarios);
        cuenta = new GestoPagoCuenta();
        cuenta.setId(7);
        cuenta.setSaldo(new BigDecimal("100.00"));
        cuenta.setSaldoRetenido(new BigDecimal("30.00"));
        pago = new GestoPagoPago();
        pago.setId(12L);
        pago.setCuenta(cuenta);
        pago.setMontoPago(new BigDecimal("30.00"));
    }

    @Test
    void exitoDescuentaSaldoYLiberaRetencionUnaSolaVez() {
        bloquearPago();
        prepararLiquidacion();

        servicio.registrarEnvio(12L, new GestoPagoRespuestaEnvio("01", "Correcto", "901", "701"));
        servicio.registrarConfirmacion(12L,
                new GestoPagoRespuestaConfirmacion("06", "Aplicada", "901", "701"));

        assertEquals(new BigDecimal("70.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("0.00"), cuenta.getSaldoRetenido());
        assertEquals(GestoPagoEstadoPago.EXITOSO, pago.getEstatus());
        assertEquals("701", pago.getNumeroAutorizacion());
        verify(cuentas, times(1)).findByIdParaLiquidar(7);
    }

    @Test
    void rechazoLiberaRetencionSinDescontarSaldo() {
        bloquearPago();
        prepararLiquidacion();

        servicio.registrarEnvio(12L,
                new GestoPagoRespuestaEnvio("02", "Saldo insuficiente", null, "-1"));

        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("0.00"), cuenta.getSaldoRetenido());
        assertEquals(GestoPagoEstadoPago.FALLIDO, pago.getEstatus());
    }

    @Test
    void codigoDesconocidoConservaRetencionHastaConfirmar() {
        bloquearPago();
        prepararLiquidacion();

        servicio.registrarEnvio(12L,
                new GestoPagoRespuestaEnvio("55", "Resultado no identificado", null, "-1"));

        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("30.00"), cuenta.getSaldoRetenido());
        assertEquals(GestoPagoEstadoPago.PENDIENTE, pago.getEstatus());

        servicio.registrarConfirmacion(12L,
                new GestoPagoRespuestaConfirmacion("70", "No aplicada", "-1", "0"));

        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("0.00"), cuenta.getSaldoRetenido());
        assertEquals(GestoPagoEstadoPago.FALLIDO, pago.getEstatus());
    }

    @Test
    void respuestaInciertaConservaRetencionHastaConfirmar() {
        bloquearPago();
        prepararLiquidacion();

        servicio.registrarEnvio(12L,
                new GestoPagoRespuestaEnvio("82", "Timeout", null, "-1"));

        assertEquals(GestoPagoEstadoPago.PENDIENTE, pago.getEstatus());
        assertEquals(new BigDecimal("30.00"), cuenta.getSaldoRetenido());

        servicio.registrarConfirmacion(12L,
                new GestoPagoRespuestaConfirmacion("06", "Aplicada", "901", "701"));

        assertEquals(GestoPagoEstadoPago.EXITOSO, pago.getEstatus());
        assertEquals(new BigDecimal("70.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("0.00"), cuenta.getSaldoRetenido());
    }

    @Test
    void confirmacion70MarcaFalloSinDescontar() {
        bloquearPago();
        prepararLiquidacion();

        servicio.registrarConfirmacion(12L,
                new GestoPagoRespuestaConfirmacion("70", "No aplicada", "-1", "0"));

        assertEquals(GestoPagoEstadoPago.FALLIDO, pago.getEstatus());
        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("0.00"), cuenta.getSaldoRetenido());
    }

    @Test
    void confirmacionSoloDespuesDeEsperaYDentroDeVigencia() {
        bloquearPago();
        pago.setRandomProveedor("random");
        pago.setSignedProveedor("signed");
        pago.setFechaEnvio(Instant.now().minusSeconds(60));
        assertTrue(servicio.iniciarConfirmacion(12L).isEmpty());

        pago.setFechaEnvio(Instant.now().minusSeconds(100));
        assertTrue(servicio.iniciarConfirmacion(12L).isPresent());
        assertTrue(servicio.iniciarConfirmacion(12L).isEmpty());

        pago.setFechaUltimaConfirmacion(null);
        pago.setFechaEnvio(Instant.now().minusSeconds(86_500));
        assertFalse(servicio.iniciarConfirmacion(12L).isPresent());
    }

    @Test
    void falloLocalAntesDelEnvioLiberaLaReserva() {
        bloquearPago();
        prepararLiquidacion();

        servicio.cancelarSinEnvio(12L);

        assertEquals(GestoPagoEstadoPago.FALLIDO, pago.getEstatus());
        assertEquals(new BigDecimal("0.00"), cuenta.getSaldoRetenido());
        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
    }

    @Test
    void limpiezaNoLiberaPagoQueYaPudoLlegarAlProveedor() {
        bloquearPago();
        pago.setFechaEnvio(Instant.now().minusSeconds(300));

        servicio.cancelarSinEnvio(12L);

        assertEquals(GestoPagoEstadoPago.PENDIENTE, pago.getEstatus());
        assertEquals(new BigDecimal("30.00"), cuenta.getSaldoRetenido());
    }

    @Test
    void confirmacionInciertaMantieneLaRetencion() {
        bloquearPago();

        servicio.registrarConfirmacion(12L,
                new GestoPagoRespuestaConfirmacion("82", "Sin resultado", null, "-1"));

        assertEquals(GestoPagoEstadoPago.PENDIENTE, pago.getEstatus());
        assertEquals(new BigDecimal("30.00"), cuenta.getSaldoRetenido());
    }

    @Test
    void duplicadoSendTxNoSeLiquidaSinConfirmTx() {
        bloquearPago();

        servicio.registrarEnvio(12L,
                new GestoPagoRespuestaEnvio("06", "Aplicada anteriormente", "901", "701"));

        assertEquals(GestoPagoEstadoPago.PENDIENTE, pago.getEstatus());
        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("30.00"), cuenta.getSaldoRetenido());
    }

    @Test
    void inicioGuardaFormularioExactoAntesDeLlamarProveedor() {
        bloquearPago();
        GestoPagoFormularioTx formulario = new GestoPagoFormularioTx("random-original", "signed-original");

        servicio.iniciarEnvio(12L, formulario);

        assertEquals("random-original", pago.getRandomProveedor());
        assertEquals("signed-original", pago.getSignedProveedor());
        assertTrue(pago.getFechaEnvio() != null);
    }

    @Test
    void consultaSoloPermitePagosDelClienteAutenticado() {
        GestoPagoCliente propietario = new GestoPagoCliente();
        propietario.setId(7);
        cuenta.setCliente(propietario);
        pago.setUpc("P12345678901234");
        GestoPagoCliente otroCliente = new GestoPagoCliente();
        otroCliente.setId(8);
        GestoPagoUsuario usuario = new GestoPagoUsuario();
        usuario.setCliente(otroCliente);
        when(usuarios.findById(4)).thenReturn(Optional.of(usuario));
        when(pagos.findByUpc("P12345678901234")).thenReturn(Optional.of(pago));

        assertThrows(GestoPagoPagoNoEncontradoException.class,
                () -> servicio.consultar("P12345678901234", 4));
    }

    private void prepararLiquidacion() {
        when(cuentas.findByIdParaLiquidar(7)).thenReturn(Optional.of(cuenta));
    }

    private void bloquearPago() {
        when(pagos.findByIdParaActualizar(12L)).thenReturn(Optional.of(pago));
    }
}
