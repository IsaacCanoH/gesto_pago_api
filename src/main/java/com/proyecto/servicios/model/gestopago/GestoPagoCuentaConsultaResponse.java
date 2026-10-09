package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter 
@AllArgsConstructor 
public class GestoPagoCuentaConsultaResponse {
    private final String numeroCuenta;
    private final BigDecimal saldo;
    private final String estatus;
}
