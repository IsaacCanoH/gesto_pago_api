package com.proyecto.servicios.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;

import com.proyecto.servicios.model.gestopago.GestoPagoClienteConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteResumenResponse;

public interface GestoPagoClienteConsultaService {
    GestoPagoClienteConsultaResponse consultarCliente(Integer id);

    GestoPagoClienteConsultaResponse consultarClientePorCurp(String curp);

    GestoPagoClienteConsultaResponse consultarClientePorRfc(String rfc);

    GestoPagoClienteConsultaResponse consultarClientePorNumeroCuenta(String numeroCuenta);

    GestoPagoClienteConsultaResponse consultarClientePorCorreo(String correo);

    Page<GestoPagoClienteResumenResponse> consultarClientes(int pagina, int tamanio);

    Page<GestoPagoClienteResumenResponse> consultarClientesActivos(int pagina, int tamanio);

    Page<GestoPagoClienteResumenResponse> consultarClientesPorFechaCreacion(
            LocalDate desde, LocalDate hasta, int pagina, int tamanio);
}
