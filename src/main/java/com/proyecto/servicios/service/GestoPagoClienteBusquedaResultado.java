package com.proyecto.servicios.service;

import org.springframework.data.domain.Page;

import com.proyecto.servicios.model.gestopago.GestoPagoClienteConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteResumenResponse;

public record GestoPagoClienteBusquedaResultado(
        Page<GestoPagoClienteResumenResponse> resultados,
        GestoPagoClienteConsultaResponse detalle) {
}
