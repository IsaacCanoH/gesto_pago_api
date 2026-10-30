package com.proyecto.servicios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.config.GestoPagoCifradoProperties;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.model.gestopago.GestoPagoSendTxRequest;

class GestoPagoCifradorPagoTest {
    private static final String LLAVE = "HPo7OLqB4Fkk4E2yGOtwqw8H5fHR9kNx67OR4g4UdlA=";
    private static final String IV = "p5ldmBPdd/9pjC0bDC/nSg==";
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void cifraJsonConAes256CbcYOmiteCamposNulos() throws Exception {
        GestoPagoSendTxRequest request = new GestoPagoSendTxRequest(
                205L, 76L, "1111111111", "20261020120000", "25720381",
                new BigDecimal("601.99"), "P12345678901234", "083");
        String signed = new GestoPagoCifradorPago(
                mapper, propiedades(LLAVE, IV))
                .cifrar(request);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(Base64.getDecoder().decode(LLAVE), "AES"),
                new IvParameterSpec(Base64.getDecoder().decode(IV)));
        String json = new String(cipher.doFinal(Base64.getDecoder().decode(signed)), StandardCharsets.UTF_8);
        JsonNode payload = mapper.readTree(json);

        assertEquals(205, payload.get("idProducto").asInt());
        assertEquals("601.99", payload.get("montoPago").asText());
        assertEquals("P12345678901234", payload.get("upc").asText());
        assertFalse(payload.has("datoInexistente"));
    }

    @Test
    void rechazaLlaveNoConfigurada() {
        GestoPagoCifradorPago cifrador = new GestoPagoCifradorPago(
                mapper, propiedades("", IV));

        assertThrows(IllegalStateException.class,
                () -> cifrador.cifrar(new GestoPagoSendTxRequest(
                        1L, 2L, "1111111111", "20261020120000", null,
                        null, "P123", "083")));
    }

    @Test
    void omiteMontoProveedorParaProductoSinReferencia() throws Exception {
        GestoPagoPago pago = new GestoPagoPago();
        pago.setIdProductoProveedor(123L);
        pago.setIdServicioProveedor(456L);
        pago.setTelefono("5512345678");
        pago.setMontoPago(new BigDecimal("40.00"));
        pago.setHoraLocal("20261020120000");
        pago.setUpc("P12345678901234");
        pago.setUnidad("083");

        JsonNode json = mapper.readTree(mapper.writeValueAsString(GestoPagoSendTxRequest.desde(pago)));

        assertFalse(json.has("montoPago"));
        assertFalse(json.has("referencia"));
    }

    private GestoPagoCifradoProperties propiedades(String llave, String iv) {
        GestoPagoCifradoProperties propiedades = new GestoPagoCifradoProperties();
        propiedades.setSecretKey(llave);
        propiedades.setIv(iv);
        return propiedades;
    }
}
