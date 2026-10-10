package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
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

import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoAccesoDenegadoException;
import com.proyecto.servicios.exception.GestoPagoClienteInvalidoException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoCancelableException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoEncontradaException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GestoPagoCuentaServiceImplTest {
    @Mock private GestoPagoCuentaRepository cuentas;
    @Mock private GestoPagoUsuarioRepository usuarios;

    private final GestoPagoClienteMapper mapper = Mappers.getMapper(GestoPagoClienteMapper.class);

    @Test
    void consultaSaldo() {
        var cuenta = cuenta();
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta()))
                .thenReturn(Optional.of(cuenta));

        var respuesta = new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                .consultarCuenta(cuenta.getNumeroCuenta());

        assertEquals(cuenta.getNumeroCuenta(), respuesta.getNumeroCuenta());
        assertEquals(new BigDecimal("150.00"), respuesta.getSaldo());
        assertEquals("ACTIVA", respuesta.getEstatus());
    }

    @Test
    void cuentaInexistente() {
        when(cuentas.findByNumeroCuenta("desconocida")).thenReturn(Optional.empty());

        assertThrows(GestoPagoCuentaNoEncontradaException.class,
                () -> new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                        .consultarCuenta("desconocida"));
    }

    @Test
    void listaCuentasActivas() {
        var cuenta = cuenta();
        when(cuentas.findByEstatusAndCliente_ActivaTrue(eq("ACTIVA"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cuenta)));

        var pagina = new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                .consultarCuentasActivas(0, 20);

        assertEquals(1, pagina.getTotalElements());
        assertEquals(cuenta.getNumeroCuenta(), pagina.getContent().get(0).getNumeroCuenta());
    }

    @Test
    void rechazaTamanioInvalido() {
        assertThrows(GestoPagoClienteInvalidoException.class,
                () -> new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                        .consultarCuentasActivas(0, 101));
        verifyNoInteractions(cuentas);
    }

    private GestoPagoCuenta cuenta() {
        var cuenta = new GestoPagoCuenta();
        var cliente = new GestoPagoCliente();
        cliente.setId(7);
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta("00000000000000000007");
        cuenta.setSaldo(new BigDecimal("150.00"));
        return cuenta;
    }

    @Test
    void cancelaSuCuenta() {
        var cuenta = cuenta();
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstatus("ACTIVA");
        var usuario = usuario(7);
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));
        when(usuarios.findById(5)).thenReturn(Optional.of(usuario));

        new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                .cancelarCuenta(cuenta.getNumeroCuenta(), 5);

        assertEquals("INACTIVA", cuenta.getEstatus());
        assertTrue(usuario.isActivo());
        assertTrue(usuario.getCliente().isActiva());
    }

    @Test
    void cancelaCuentaSinToken() {
        var cuenta = cuenta();
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstatus("ACTIVA");
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));

        new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                .cancelarCuenta(cuenta.getNumeroCuenta(), null);

        assertEquals("INACTIVA", cuenta.getEstatus());
        verifyNoInteractions(usuarios);
    }

    @Test
    void noCancelaCuentaAjena() {
        var cuenta = cuenta();
        cuenta.setEstatus("ACTIVA");
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));
        when(usuarios.findById(5)).thenReturn(Optional.of(usuario(8)));

        assertThrows(GestoPagoAccesoDenegadoException.class,
                () -> new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                        .cancelarCuenta(cuenta.getNumeroCuenta(), 5));
        assertEquals("ACTIVA", cuenta.getEstatus());
    }

    @Test
    void noCancelaConSaldo() {
        var cuenta = cuenta();
        cuenta.setEstatus("ACTIVA");
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));
        when(usuarios.findById(5)).thenReturn(Optional.of(usuario(7)));

        assertThrows(GestoPagoCuentaNoCancelableException.class,
                () -> new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                        .cancelarCuenta(cuenta.getNumeroCuenta(), 5));
        assertEquals("ACTIVA", cuenta.getEstatus());
    }

    @Test
    void cancelarDosVecesNoCambiaNada() {
        var cuenta = cuenta();
        cuenta.setEstatus("INACTIVA");
        when(cuentas.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));
        when(usuarios.findById(5)).thenReturn(Optional.of(usuario(7)));

        new GestoPagoCuentaServiceImpl(cuentas, usuarios, mapper)
                .cancelarCuenta(cuenta.getNumeroCuenta(), 5);

        assertEquals("INACTIVA", cuenta.getEstatus());
    }

    private GestoPagoUsuario usuario(int clienteId) {
        var cliente = new GestoPagoCliente();
        cliente.setId(clienteId);
        var usuario = new GestoPagoUsuario();
        usuario.setCliente(cliente);
        return usuario;
    }
}
