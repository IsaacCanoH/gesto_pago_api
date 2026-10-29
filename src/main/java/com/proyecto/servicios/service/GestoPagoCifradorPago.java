package com.proyecto.servicios.service;

import java.util.Base64;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.config.GestoPagoCifradoProperties;
import com.proyecto.servicios.model.gestopago.GestoPagoSendTxRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GestoPagoCifradorPago {
    private final ObjectMapper objectMapper;
    private final GestoPagoCifradoProperties properties;

    public String cifrar(GestoPagoSendTxRequest request) {
        byte[] llave = decodificar(properties.getSecretKey(), 32, "GESTOPAGO_PAGO_SECRET_KEY");
        byte[] iv = decodificar(properties.getIv(), 16, "GESTOPAGO_PAGO_IV");

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(llave, "AES"), new IvParameterSpec(iv));
            byte[] json = objectMapper.writeValueAsBytes(request);
            return Base64.getEncoder().encodeToString(cipher.doFinal(json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No fue posible preparar la solicitud del pago", e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible cifrar la solicitud del pago", e);
        }
    }

    private byte[] decodificar(String valor, int longitud, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(nombre + " no está configurada");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(valor);
            if (bytes.length != longitud) {
                throw new IllegalArgumentException("Longitud inválida");
            }
            return bytes;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(nombre + " debe estar en Base64 y tener " + longitud + " bytes", e);
        }
    }
}
