package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.web.PagedModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GestoPagoClienteBusquedaResponse(
        PagedModel<GestoPagoClienteResumenResponse> resultados,
        GestoPagoClienteConsultaResponse detalle) {
}
