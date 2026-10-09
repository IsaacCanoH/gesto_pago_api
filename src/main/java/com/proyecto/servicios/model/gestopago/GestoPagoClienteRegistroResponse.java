package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GestoPagoClienteRegistroResponse {

    private final Integer clienteId;
    private final String numeroCuenta;
    private final BigDecimal saldoInicial;
}