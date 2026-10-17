package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoClienteInvalidoException;
import com.proyecto.servicios.exception.GestoPagoClienteNoEncontradoException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteBusquedaRequest;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoClienteRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoDomicilioRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GestoPagoClienteConsultaServiceImplTest {
    @Mock private GestoPagoClienteRepository clientes;
    @Mock private GestoPagoDomicilioRepository domicilios;
    @Mock private GestoPagoCuentaRepository cuentas;
    @Mock private GestoPagoUsuarioRepository usuarios;

    private final GestoPagoClienteMapper mapper = Mappers.getMapper(GestoPagoClienteMapper.class);
    private GestoPagoClienteConsultaServiceImpl service;
    private GestoPagoCliente cliente;
    private GestoPagoUsuario usuario;

    @BeforeEach
    void preparar() {
        service = new GestoPagoClienteConsultaServiceImpl(
                clientes, domicilios, cuentas, usuarios, mapper);
        cliente = new GestoPagoCliente();
        cliente.setId(7);
        usuario = new GestoPagoUsuario();
        usuario.setCliente(cliente);
        usuario.setCorreo("cliente@example.com");
    }

    @Test
    void listaResumidaConFiltrosCombinables() {
        var request = new GestoPagoClienteBusquedaRequest();
        request.setCurp("can");
        request.setRfc("5g3");
        request.setParcial(true);
        request.setActiva(true);
        when(clientes.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente)));
        when(usuarios.findByCliente_IdIn(List.of(7))).thenReturn(List.of(usuario));

        var resultado = service.buscarClientes(request);

        assertEquals(1, resultado.resultados().getTotalElements());
        assertEquals("cliente@example.com", resultado.resultados().getContent().get(0).correo());
        assertNull(resultado.detalle());
        verifyNoInteractions(domicilios, cuentas);
    }

    @Test
    void agregaDetalleCuandoLaBusquedaIdentificaUnCliente() {
        var request = new GestoPagoClienteBusquedaRequest();
        request.setId(7);
        request.setDetalle(true);
        when(clientes.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente)));
        when(usuarios.findByCliente_IdIn(List.of(7))).thenReturn(List.of(usuario));
        when(clientes.findById(7)).thenReturn(Optional.of(cliente));
        var domicilio = new GestoPagoDomicilio();
        domicilio.setCliente(cliente);
        when(domicilios.findByCliente_Id(7)).thenReturn(Optional.of(domicilio));
        when(usuarios.findByCliente_Id(7)).thenReturn(Optional.of(usuario));
        var cuenta = new GestoPagoCuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta("00000000000000000007");
        when(cuentas.findByCliente_Id(7)).thenReturn(List.of(cuenta));

        var resultado = service.buscarClientes(request);

        assertEquals(7, resultado.detalle().getId());
        assertEquals("00000000000000000007",
                resultado.detalle().getCuentas().get(0).getNumeroCuenta());
    }

    @Test
    void detalleInexistenteDevuelveNoEncontrado() {
        var request = new GestoPagoClienteBusquedaRequest();
        request.setId(99);
        request.setDetalle(true);
        when(clientes.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThrows(GestoPagoClienteNoEncontradoException.class,
                () -> service.buscarClientes(request));
    }

    @Test
    void detalleAmbiguoSeRechaza() {
        var request = new GestoPagoClienteBusquedaRequest();
        request.setDetalle(true);
        when(clientes.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente), PageRequest.of(0, 1), 2));
        when(usuarios.findByCliente_IdIn(List.of(7))).thenReturn(List.of(usuario));

        assertThrows(GestoPagoClienteInvalidoException.class,
                () -> service.buscarClientes(request));
    }

    @Test
    void rangoInvertidoSeRechazaAntesDeConsultar() {
        var request = new GestoPagoClienteBusquedaRequest();
        request.setDesde(LocalDate.of(2026, 10, 6));
        request.setHasta(LocalDate.of(2026, 10, 5));

        assertThrows(GestoPagoClienteInvalidoException.class,
                () -> service.buscarClientes(request));
        verifyNoInteractions(clientes);
    }
}
