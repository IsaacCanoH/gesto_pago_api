package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoAccesoDenegadoException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoCancelableException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoEncontradaException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaBusquedaRequest;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GestoPagoCuentaServiceImplTest {
    @Mock private GestoPagoCuentaRepository cuentas;
    @Mock private GestoPagoUsuarioRepository usuarios;
    private final GestoPagoClienteMapper mapper = Mappers.getMapper(GestoPagoClienteMapper.class);

    @Test
    void buscaCuentasPorNumeroYEstado() {
        var request = new GestoPagoCuentaBusquedaRequest();
        request.setNumeroCuenta("00000000000000000007");
        request.setSoloActivas(true);
        when(cuentas.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cuenta())));

        var resultado = servicio().buscarCuentas(request);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("00000000000000000007", resultado.getContent().get(0).getNumeroCuenta());
    }

    @Test
    void numeroDeCuentaInexistente() {
        var request = new GestoPagoCuentaBusquedaRequest();
        request.setNumeroCuenta("00000000000000000099");
        when(cuentas.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThrows(GestoPagoCuentaNoEncontradaException.class,
                () -> servicio().buscarCuentas(request));
    }

    @Test
    void cancelaSuCuentaSinDesactivarUsuario() {
        var cuenta = cuenta();
        cuenta.setSaldo(BigDecimal.ZERO);
        var usuario = usuario(7);
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));
        when(usuarios.findById(5)).thenReturn(Optional.of(usuario));

        servicio().cancelarCuenta(cuenta.getNumeroCuenta(), 5);

        assertEquals("INACTIVA", cuenta.getEstatus());
        assertTrue(usuario.isActivo());
        assertTrue(usuario.getCliente().isActiva());
    }

    @Test
    void noCancelaCuentaAjena() {
        var cuenta = cuenta();
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));
        when(usuarios.findById(5)).thenReturn(Optional.of(usuario(8)));

        assertThrows(GestoPagoAccesoDenegadoException.class,
                () -> servicio().cancelarCuenta(cuenta.getNumeroCuenta(), 5));
        assertEquals("ACTIVA", cuenta.getEstatus());
    }

    @Test
    void noCancelaCuentaConSaldo() {
        var cuenta = cuenta();
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));
        when(usuarios.findById(5)).thenReturn(Optional.of(usuario(7)));

        assertThrows(GestoPagoCuentaNoCancelableException.class,
                () -> servicio().cancelarCuenta(cuenta.getNumeroCuenta(), 5));
    }

    private GestoPagoCuentaServiceImpl servicio() {
        return new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper);
    }

    private GestoPagoCuenta cuenta() {
        var cliente = new GestoPagoCliente();
        cliente.setId(7);
        var cuenta = new GestoPagoCuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta("00000000000000000007");
        cuenta.setSaldo(new BigDecimal("150.00"));
        cuenta.setEstatus("ACTIVA");
        return cuenta;
    }

    private GestoPagoUsuario usuario(int clienteId) {
        var cliente = new GestoPagoCliente();
        cliente.setId(clienteId);
        var usuario = new GestoPagoUsuario();
        usuario.setCliente(cliente);
        return usuario;
    }
}
