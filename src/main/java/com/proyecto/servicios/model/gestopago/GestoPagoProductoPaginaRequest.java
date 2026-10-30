package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GestoPagoProductoPaginaRequest {
    @Min(value = 0, message = "debe ser mayor o igual a cero")
    private int pagina = 0;

    @Min(value = 1, message = "debe ser mayor o igual a uno")
    @Max(value = 100, message = "no debe superar 100")
    private int tamanio = 20;
}
