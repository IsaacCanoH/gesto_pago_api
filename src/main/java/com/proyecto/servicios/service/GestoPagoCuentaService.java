package com.proyecto.servicios.service;

import org.springframework.data.domain.Page;

import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;

public interface GestoPagoCuentaService {
    GestoPagoCuentaConsultaResponse consultarCuenta(String numeroCuenta);

    Page<GestoPagoCuentaConsultaResponse> consultarCuentasActivas(int pagina, int tamanio);

    void cancelarCuenta(String numeroCuenta, Integer usuarioId);
} 
