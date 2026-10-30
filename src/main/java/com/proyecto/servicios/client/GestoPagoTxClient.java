package com.proyecto.servicios.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gestoPagoTx", url = "${gestopago.base-url}")
public interface GestoPagoTxClient {

    @PostMapping(value = "${gestopago.pago.endpoint:/sistema/service/sendTx.do}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String sendTx(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestBody MultiValueMap<String, String> formulario);

    @PostMapping(value = "${gestopago.pago.confirm-endpoint:/sistema/service/confirmTx.do}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String confirmTx(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestBody MultiValueMap<String, String> formulario);
}
