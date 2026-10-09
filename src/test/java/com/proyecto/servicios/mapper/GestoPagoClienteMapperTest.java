package com.proyecto.servicios.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteActualizacionRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteParcialRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoDomicilioParcialRequest;

class GestoPagoClienteMapperTest {
    private final GestoPagoClienteMapper mapper = Mappers.getMapper(GestoPagoClienteMapper.class);

    @Test
    void creaClienteActivo() {
        var request = new GestoPagoClienteRegistroRequest();
        request.setNombre("María");
        request.setCurp("CANO050429HKNTEW00");
        request.setRfc("CANO0504295G3");

        var cliente = mapper.toCliente(request);

        assertEquals("María", cliente.getNombre());
        assertEquals(request.getCurp(), cliente.getCurp());
        assertEquals(request.getRfc(), cliente.getRfc());
        assertTrue(cliente.isActiva());
    }

    @Test
    void conservaCurpYRfcAlActualizar() {
        var cliente = cliente();
        var request = new GestoPagoClienteActualizacionRequest();
        request.setNombre("María");
        request.setTelefonoMovil("5512345678");
        request.setOcupacion("Analista");
        request.setIngresoMensual(new BigDecimal("9000.00"));

        mapper.actualizarCliente(request, cliente);

        assertEquals(7, cliente.getId());
        assertEquals("María", cliente.getNombre());
        assertEquals("5512345678", cliente.getTelefonoMovil());
        assertEquals("Analista", cliente.getOcupacion());
        assertEquals(new BigDecimal("9000.00"), cliente.getIngresoMensual());
        assertEquals("CANO050429HKNTEW00", cliente.getCurp());
        assertEquals("CANO0504295G3", cliente.getRfc());
    }

    @Test
    void cambioParcialConservaLoDemas() {
        var cliente = cliente();
        cliente.setNombre("Isaac");
        var request = new GestoPagoClienteParcialRequest();
        request.setOcupacion("Tester");

        mapper.actualizarClienteParcial(request, cliente);

        assertEquals("Isaac", cliente.getNombre());
        assertEquals("Tester", cliente.getOcupacion());
        assertEquals("CANO050429HKNTEW00", cliente.getCurp());
        assertEquals("CANO0504295G3", cliente.getRfc());
    }

    @Test
    void cambiaSoloLaCalle() {
        var domicilio = new GestoPagoDomicilio();
        domicilio.setCalle("Calle Reforma");
        domicilio.setNumeroExterior("3B");
        var request = new GestoPagoDomicilioParcialRequest();
        request.setCalle("Calle Hidalgo");

        mapper.actualizarDomicilioParcial(request, domicilio);

        assertEquals("Calle Hidalgo", domicilio.getCalle());
        assertEquals("3B", domicilio.getNumeroExterior());
    }

    private GestoPagoCliente cliente() {
        var cliente = new GestoPagoCliente();
        cliente.setId(7);
        cliente.setCurp("CANO050429HKNTEW00");
        cliente.setRfc("CANO0504295G3");
        return cliente;
    }
}
