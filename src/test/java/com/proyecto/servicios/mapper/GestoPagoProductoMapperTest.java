package com.proyecto.servicios.mapper;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse.Producto;

class GestoPagoProductoMapperTest {
    @Test
    void eliminaPrecioAntiguoCuandoElCatalogoYaNoLoPublica() {
        GestoPagoProducto existente = new GestoPagoProducto();
        existente.setPrecio(new BigDecimal("100.00"));
        Producto recibido = new Producto();
        recibido.setIdProducto(1L);
        recibido.setServicio("Servicio");
        recibido.setProducto("Producto");

        Mappers.getMapper(GestoPagoProductoMapper.class).updateEntity(recibido, existente);

        assertNull(existente.getPrecio());
    }
}
