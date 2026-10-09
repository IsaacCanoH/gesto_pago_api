package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoClienteActualizacionRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteParcialRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroResponse;

public interface GestoPagoClienteService {
    GestoPagoClienteRegistroResponse registrarCliente(GestoPagoClienteRegistroRequest request);

    void actualizarCliente(
            Integer id, GestoPagoClienteActualizacionRequest request);

    void actualizarClienteParcial(Integer id, GestoPagoClienteParcialRequest request);

    void desactivarCliente(Integer id);
}
