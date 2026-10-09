package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoClienteInvalidoException;
import com.proyecto.servicios.exception.GestoPagoClienteNoEncontradoException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoEncontradaException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
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
    private GestoPagoDomicilio domicilio;
    private GestoPagoCuenta cuenta;
    private GestoPagoUsuario usuario;

    @BeforeEach
    void datos() {
        service = new GestoPagoClienteConsultaServiceImpl(
                clientes, domicilios, cuentas, usuarios, mapper);
        cliente = new GestoPagoCliente();
        cliente.setId(7);
        domicilio = new GestoPagoDomicilio();
        domicilio.setCliente(cliente);
        cuenta = new GestoPagoCuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta("00000000000000000007");
        usuario = new GestoPagoUsuario();
        usuario.setCliente(cliente);
        usuario.setCorreo("cliente@example.com");
    }

    @Test
    void buscaPorId() {
        prepararDetalle();

        var respuesta = service.consultarCliente(7);

        assertEquals(7, respuesta.getId());
        assertEquals("cliente@example.com", respuesta.getCorreo());
        assertEquals("00000000000000000007", respuesta.getCuentas().get(0).getNumeroCuenta());
    }

    @Test
    void buscaPorCurp() {
        when(clientes.findByCurp("CANO050429HKNTEW00")).thenReturn(Optional.of(cliente));
        prepararDetalle();

        assertEquals(7, service.consultarClientePorCurp("CANO050429HKNTEW00").getId());
    }

    @Test
    void buscaPorRfc() {
        when(clientes.findByRfc("CANO0504295G3")).thenReturn(Optional.of(cliente));
        prepararDetalle();

        assertEquals(7, service.consultarClientePorRfc("CANO0504295G3").getId());
    }

    @Test
    void buscaPorCuenta() {
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));
        prepararDetalle();

        assertEquals(7, service.consultarClientePorNumeroCuenta(cuenta.getNumeroCuenta()).getId());
    }

    @Test
    void buscaPorCorreo() {
        when(usuarios.findByCorreoIgnoreCase("CLIENTE@example.com"))
                .thenReturn(Optional.of(usuario));
        prepararDetalle();

        assertEquals(7, service.consultarClientePorCorreo("CLIENTE@example.com").getId());
    }

    @Test
    void listaClientes() {
        when(clientes.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente)));
        prepararPagina();

        var pagina = service.consultarClientes(0, 20);

        assertEquals(1, pagina.getTotalElements());
        assertEquals("cliente@example.com", pagina.getContent().get(0).correo());
        assertEquals(7, pagina.getContent().get(0).id());
        verifyNoInteractions(domicilios, cuentas);
        verify(usuarios).findByCliente_IdIn(List.of(7));
    }

    @Test
    void listaActivos() {
        when(clientes.findByActivaTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente)));
        prepararPagina();

        assertEquals(1, service.consultarClientesActivos(0, 20).getNumberOfElements());
    }

    @Test
    void filtraPorFecha() {
        LocalDate dia = LocalDate.of(2026, 10, 5);
        when(clientes.findByFechaCreacionBetween(eq(dia), eq(dia), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente)));
        prepararPagina();

        assertEquals(1,
                service.consultarClientesPorFechaCreacion(dia, dia, 0, 20).getNumberOfElements());
    }

    @Test
    void rechazaRangoInvertido() {
        assertThrows(GestoPagoClienteInvalidoException.class,
                () -> service.consultarClientesPorFechaCreacion(
                        LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 5), 0, 20));
        verifyNoInteractions(clientes);
    }

    @Test
    void rechazaPaginaInvalida() {
        assertThrows(GestoPagoClienteInvalidoException.class,
                () -> service.consultarClientes(-1, 20));
        assertThrows(GestoPagoClienteInvalidoException.class,
                () -> service.consultarClientes(0, 101));
        verifyNoInteractions(clientes);
    }

    @Test
    void clienteInexistente() {
        when(clientes.findById(99)).thenReturn(Optional.empty());

        assertThrows(GestoPagoClienteNoEncontradoException.class,
                () -> service.consultarCliente(99));
    }

    @Test
    void cuentaInexistente() {
        when(cuentas.findByNumeroCuenta("desconocida")).thenReturn(Optional.empty());

        assertThrows(GestoPagoCuentaNoEncontradaException.class,
                () -> service.consultarClientePorNumeroCuenta("desconocida"));
    }

    private void prepararDetalle() {
        when(clientes.findById(7)).thenReturn(Optional.of(cliente));
        when(domicilios.findByCliente_Id(7)).thenReturn(Optional.of(domicilio));
        when(cuentas.findByCliente_Id(7)).thenReturn(List.of(cuenta));
        when(usuarios.findByCliente_Id(7)).thenReturn(Optional.of(usuario));
    }

    private void prepararPagina() {
        when(usuarios.findByCliente_IdIn(List.of(7))).thenReturn(List.of(usuario));
    }
}
