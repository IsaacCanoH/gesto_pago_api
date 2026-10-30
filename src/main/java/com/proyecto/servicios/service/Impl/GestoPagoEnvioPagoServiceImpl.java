package com.proyecto.servicios.service.Impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.proyecto.servicios.client.GestoPagoTxClient;
import com.proyecto.servicios.config.GestoPagoCifradoProperties;
import com.proyecto.servicios.config.GestoPagoIdentidadProveedorProperties;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoExternalResponseException;
import com.proyecto.servicios.exception.GestoPagoPagoIndeterminadoException;
import com.proyecto.servicios.exception.GestoPagoPagoInvalidoException;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.model.gestopago.GestoPagoFormularioTx;
import com.proyecto.servicios.model.gestopago.GestoPagoSendTxRequest;
import com.proyecto.servicios.service.GestoPagoCifradorPago;
import com.proyecto.servicios.service.GestoPagoEnvioPagoService;
import com.proyecto.servicios.service.GestoPagoRespuestaEnvioParser;
import com.proyecto.servicios.service.GestoPagoTokenService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestoPagoEnvioPagoServiceImpl implements GestoPagoEnvioPagoService {
    private final GestoPagoTxClient txClient;
    private final GestoPagoTokenService tokenService;
    private final GestoPagoCifradorPago cifrador;
    private final GestoPagoRespuestaEnvioParser parser;
    private final GestoPagoCifradoProperties cifradoProperties;
    private final GestoPagoIdentidadProveedorProperties identidad;

    @Override
    public GestoPagoFormularioTx preparar(GestoPagoPago pago) {
        if (pago == null || pago.getEstatus() != GestoPagoEstadoPago.PENDIENTE) {
            throw new GestoPagoPagoInvalidoException("Solo se puede preparar un pago pendiente");
        }
        return new GestoPagoFormularioTx(
                UUID.randomUUID().toString().replace("-", ""),
                cifrador.cifrar(GestoPagoSendTxRequest.desde(pago)));
    }

    @Override
    public GestoPagoRespuestaEnvio enviar(GestoPagoPago pago) {
        String xml = solicitar(pago, Operacion.ENVIAR);
        GestoPagoRespuestaEnvio resultado;
        try {
            resultado = parser.parsear(xml);
        } catch (GestoPagoExternalResponseException e) {
            throw new GestoPagoPagoIndeterminadoException(e);
        }
        log.info("Respuesta sendTx UPC={} código={}", pago.getUpc(), resultado.codigo());
        return resultado;
    }

    @Override
    public GestoPagoRespuestaConfirmacion confirmar(GestoPagoPago pago) {
        String xml = solicitar(pago, Operacion.CONFIRMAR);
        GestoPagoRespuestaConfirmacion resultado;
        try {
            resultado = parser.parsearConfirmacion(xml);
        } catch (GestoPagoExternalResponseException e) {
            throw new GestoPagoPagoIndeterminadoException(e);
        }
        log.info("Respuesta confirmTx UPC={} código={}", pago.getUpc(), resultado.codigo());
        return resultado;
    }

    private String solicitar(GestoPagoPago pago, Operacion operacion) {
        if (pago == null || pago.getEstatus() != GestoPagoEstadoPago.PENDIENTE
                || pago.getRandomProveedor() == null || pago.getSignedProveedor() == null) {
            throw new GestoPagoPagoInvalidoException("El pago pendiente no tiene una solicitud preparada");
        }
        GestoPagoToken token = obtenerToken();

        try {
            return llamarProveedor(token, pago, operacion);
        } catch (FeignException e) {
            if (operacion == Operacion.ENVIAR || (e.status() != 401 && e.status() != 403)) {
                throw new GestoPagoPagoIndeterminadoException(e);
            }
            log.warn("Token rechazado al consultar UPC={}; se renovará para confirmar", pago.getUpc());
            try {
                return llamarProveedor(tokenService.renovarTokenAhora(), pago, operacion);
            } catch (FeignException segundoIntento) {
                throw new GestoPagoPagoIndeterminadoException(segundoIntento);
            }
        }
    }

    private GestoPagoToken obtenerToken() {
        return tokenService.obtenerTokenActivo(
                        identidad.idDistribuidor(), identidad.codigoDispositivo())
                .filter(token -> Boolean.TRUE.equals(token.getActivo())
                        && token.getToken() != null && !token.getToken().isBlank())
                .orElseGet(tokenService::renovarTokenAhora);
    }

    private String llamarProveedor(GestoPagoToken token, GestoPagoPago pago, Operacion operacion) {
        String apiKey = cifradoProperties.getApiKey();
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("random", pago.getRandomProveedor());
        formulario.add("signed", pago.getSignedProveedor());
        String authorization = "Bearer " + token.getToken();
        String clave = apiKey == null || apiKey.isBlank() ? null : apiKey;
        return operacion == Operacion.ENVIAR
                ? txClient.sendTx(authorization, clave, formulario)
                : txClient.confirmTx(authorization, clave, formulario);
    }

    private enum Operacion {
        ENVIAR, CONFIRMAR
    }
}
