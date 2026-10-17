package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoPaginaRequest;
import java.util.List;
import org.springframework.data.domain.Page;

public interface GestoPagoProductService {

    GestoPagoProductListResponse descargarListaProductos();
    int sincronizarProductos();
    List<GestoPagoProductoResponse> consultarProductos();
    Page<GestoPagoProductoResponse> consultarProductosPaginados(GestoPagoProductoPaginaRequest request);
}
