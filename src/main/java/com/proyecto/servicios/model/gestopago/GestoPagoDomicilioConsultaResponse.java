package com.proyecto.servicios.model.gestopago;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GestoPagoDomicilioConsultaResponse {
    private final String calle;
    private final String numeroExterior;
    private final String numeroInterior;
    private final String colonia;
    private final String municipio;
    private final String estado;
    private final String codigoPostal;
    private final String pais;
}
