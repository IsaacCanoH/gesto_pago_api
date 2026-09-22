package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import java.util.List;

public interface GestoPagoProductService {

    GestoPagoProductListResponse descargarListaProductos();
    int sincronizarProductos();
    List<GestoPagoProductoResponse> consultarProductos();
}