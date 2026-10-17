package com.proyecto.servicios.service;

import org.springframework.data.domain.Page;

import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaBusquedaRequest;

public interface GestoPagoCuentaService {
    Page<GestoPagoCuentaConsultaResponse> buscarCuentas(GestoPagoCuentaBusquedaRequest request);

    void cancelarCuenta(String numeroCuenta, Integer usuarioId);
} 
