package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoClienteBusquedaRequest;

public interface GestoPagoClienteConsultaService {
    GestoPagoClienteBusquedaResultado buscarClientes(GestoPagoClienteBusquedaRequest request);
}
