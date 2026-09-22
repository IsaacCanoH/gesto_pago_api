package com.proyecto.servicios.model.gestopago;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class GestoPagoProductoResponse {

    private Long idProducto;
    private Long idServicio;
    private Long idCatTipoServicio;
    private String servicio;
    private String producto;
    private Integer tipoFront;
    private Boolean hasDigitoVerificador;
    private BigDecimal precio;
    private Boolean showAyuda;
    private String tipoReferencia;
    private String leyenda;
}