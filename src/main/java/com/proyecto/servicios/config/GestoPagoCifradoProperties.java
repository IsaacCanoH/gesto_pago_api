package com.proyecto.servicios.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "gestopago.pago")
@Getter
@Setter
public class GestoPagoCifradoProperties {
    private String secretKey = "";
    private String iv = "";
    private String apiKey = "";
}
