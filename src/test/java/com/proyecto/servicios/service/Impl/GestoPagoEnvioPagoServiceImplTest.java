package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import com.proyecto.servicios.client.GestoPagoTxClient;
import com.proyecto.servicios.config.GestoPagoCifradoProperties;
import com.proyecto.servicios.config.GestoPagoIdentidadProveedorProperties;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoPagoIndeterminadoException;
import com.proyecto.servicios.exception.GestoPagoPagoInvalidoException;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.service.GestoPagoCifradorPago;
import com.proyecto.servicios.service.GestoPagoRespuestaEnvioParser;
import com.proyecto.servicios.service.GestoPagoTokenService;

import feign.FeignException;
import feign.Request;
import feign.Response;

@ExtendWith(MockitoExtension.class)
class GestoPagoEnvioPagoServiceImplTest {
    @Mock private GestoPagoTxClient txClient;
    @Mock private GestoPagoTokenService tokenService;
    @Mock private GestoPagoCifradorPago cifrador;
    @Mock private GestoPagoRespuestaEnvioParser parser;

    private GestoPagoEnvioPagoServiceImpl servicio;

    @BeforeEach
    void preparar() {
        servicio = new GestoPagoEnvioPagoServiceImpl(txClient, tokenService, cifrador, parser,
                new GestoPagoCifradoProperties(),
                new GestoPagoIdentidadProveedorProperties(83, "GPS83-TPV-17"));
    }

    @Test
    void enviaFormularioCifradoConTokenReutilizadoYConservaCodigo() {
        prepararEnvio("token-existente");
        when(txClient.sendTx(eq("Bearer token-existente"), isNull(), any()))
                .thenReturn("<RESPONSE/>");
        when(parser.parsear("<RESPONSE/>"))
                .thenReturn(new GestoPagoRespuestaEnvio("02", "Saldo insuficiente", null, "-1"));

        GestoPagoRespuestaEnvio resultado = servicio.enviar(pagoPendiente());

        assertFalse(resultado.exitosa());
        assertEquals("02", resultado.codigo());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MultiValueMap<String, String>> formulario =
                ArgumentCaptor.forClass(MultiValueMap.class);
        verify(txClient).sendTx(eq("Bearer token-existente"), isNull(), formulario.capture());
        assertEquals("signed", formulario.getValue().getFirst("signed"));
        assertEquals("random-original", formulario.getValue().getFirst("random"));
        verify(tokenService, never()).renovarTokenAhora();
    }

    @Test
    void rechazoDeTokenEnSendTxNoReenviaElPago() {
        prepararEnvio("token-vencido");
        when(txClient.sendTx(eq("Bearer token-vencido"), isNull(), any()))
                .thenThrow(errorHttp(403));

        assertThrows(GestoPagoPagoIndeterminadoException.class,
                () -> servicio.enviar(pagoPendiente()));
        verify(txClient).sendTx(eq("Bearer token-vencido"), isNull(), any());
        verify(tokenService, never()).renovarTokenAhora();
        verifyNoInteractions(parser);
    }

    @Test
    void errorHttpNoSeReintentaComoPagoNuevo() {
        prepararEnvio("token-existente");
        when(txClient.sendTx(eq("Bearer token-existente"), isNull(), any()))
                .thenThrow(errorHttp(500));

        assertThrows(GestoPagoPagoIndeterminadoException.class,
                () -> servicio.enviar(pagoPendiente()));
        verify(tokenService, never()).renovarTokenAhora();
        verifyNoInteractions(parser);
    }

    @Test
    void noEnviaPagoQueYaNoEstaPendiente() {
        GestoPagoPago pago = pagoPendiente();
        pago.setEstatus(GestoPagoEstadoPago.EXITOSO);

        assertThrows(GestoPagoPagoInvalidoException.class, () -> servicio.enviar(pago));
        verifyNoInteractions(txClient, tokenService, cifrador, parser);
    }

    @Test
    void confirmaConElMismoFormularioSinRepetirSendTx() {
        prepararEnvio("token-existente");
        when(txClient.confirmTx(eq("Bearer token-existente"), isNull(), any()))
                .thenReturn("<RESPONSE/>");
        when(parser.parsearConfirmacion("<RESPONSE/>"))
                .thenReturn(new GestoPagoRespuestaConfirmacion("06", "Aplicada", "9", "10"));

        assertEquals("06", servicio.confirmar(pagoPendiente()).codigo());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MultiValueMap<String, String>> formulario =
                ArgumentCaptor.forClass(MultiValueMap.class);
        verify(txClient).confirmTx(eq("Bearer token-existente"), isNull(), formulario.capture());
        assertEquals("random-original", formulario.getValue().getFirst("random"));
        assertEquals("signed", formulario.getValue().getFirst("signed"));
        verify(txClient, never()).sendTx(any(), any(), any());
    }

    @Test
    void rechazoDeTokenEnConfirmTxSoloReintentaLaConsulta() {
        prepararEnvio("token-vencido");
        when(tokenService.renovarTokenAhora()).thenReturn(token("token-nuevo"));
        when(txClient.confirmTx(eq("Bearer token-vencido"), isNull(), any()))
                .thenThrow(errorHttp(401));
        when(txClient.confirmTx(eq("Bearer token-nuevo"), isNull(), any()))
                .thenReturn("<RESPONSE/>");
        when(parser.parsearConfirmacion("<RESPONSE/>"))
                .thenReturn(new GestoPagoRespuestaConfirmacion("06", "Aplicada", "9", "10"));

        assertEquals("06", servicio.confirmar(pagoPendiente()).codigo());
        verify(txClient, never()).sendTx(any(), any(), any());
    }

    @Test
    void preparaFormularioUnaSolaVezAntesDeEnviar() {
        when(cifrador.cifrar(any())).thenReturn("signed");

        var formulario = servicio.preparar(pagoPendiente());

        assertEquals("signed", formulario.signed());
        assertEquals(32, formulario.random().length());
        verifyNoInteractions(txClient, tokenService);
    }

    private void prepararEnvio(String valorToken) {
        when(tokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(token(valorToken)));
    }

    private GestoPagoToken token(String valor) {
        GestoPagoToken token = new GestoPagoToken();
        token.setToken(valor);
        token.setActivo(true);
        return token;
    }

    private GestoPagoPago pagoPendiente() {
        GestoPagoPago pago = new GestoPagoPago();
        pago.setIdServicioProveedor(76L);
        pago.setIdProductoProveedor(205L);
        pago.setTelefono("1111111111");
        pago.setReferencia("25720381");
        pago.setMontoPago(new BigDecimal("601.99"));
        pago.setHoraLocal("20261020120000");
        pago.setUpc("P12345678901234");
        pago.setUnidad("083");
        pago.setRandomProveedor("random-original");
        pago.setSignedProveedor("signed");
        return pago;
    }

    private FeignException errorHttp(int status) {
        Request request = Request.create(Request.HttpMethod.POST,
                "https://gestopago.portalventas.net/sistema/service/sendTx.do",
                Collections.emptyMap(), Request.Body.empty(), null);
        return FeignException.errorStatus("sendTx",
                Response.builder().status(status).reason("Error").request(request)
                        .headers(Collections.emptyMap()).build());
    }
}
