package com.proyecto.servicios.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import feign.Feign;
import feign.Request;
import feign.Response;
import feign.codec.StringDecoder;

class GestoPagoTxFormularioTest {
    @Test
    void colocaRandomYSignedEnCuerpoFormUrlencoded() {
        AtomicReference<Request> solicitud = new AtomicReference<>();
        Formulario cliente = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new SpringEncoder(HttpMessageConverters::new))
                .decoder(new StringDecoder())
                .client((request, options) -> {
                    solicitud.set(request);
                    return Response.builder().status(200).reason("OK")
                            .request(request).headers(Map.of())
                            .body("<RESPONSE/>", StandardCharsets.UTF_8).build();
                })
                .target(Formulario.class, "https://example.test");

        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("random", "abc123");
        formulario.add("signed", "a+/b==");
        cliente.sendTx("Bearer token", formulario);

        Request request = solicitud.get();
        String cuerpo = new String(request.body(), StandardCharsets.UTF_8);
        assertEquals("https://example.test/sendTx.do", request.url());
        assertTrue(cuerpo.contains("random=abc123"));
        assertTrue(cuerpo.contains("signed=a%2B%2Fb%3D%3D"));
        assertFalse(request.url().contains("signed="));
    }

    interface Formulario {
        @PostMapping(value = "/sendTx.do", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        String sendTx(@RequestHeader("Authorization") String token,
                @RequestBody MultiValueMap<String, String> formulario);
    }
}
