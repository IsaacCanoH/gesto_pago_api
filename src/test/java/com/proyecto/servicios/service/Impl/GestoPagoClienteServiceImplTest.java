package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoContrasenaInvalidaException;
import com.proyecto.servicios.exception.GestoPagoClienteDuplicadoException;
import com.proyecto.servicios.exception.GestoPagoClienteInvalidoException;
import com.proyecto.servicios.exception.GestoPagoClienteNoEncontradoException;
import com.proyecto.servicios.exception.GestoPagoCorreoDuplicadoException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteActualizacionRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteParcialRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoDomicilioRequest;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoClienteRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoDomicilioRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GestoPagoClienteServiceImplTest {

    @Mock
    private GestoPagoClienteRepository clienteRepository;
    @Mock
    private GestoPagoDomicilioRepository domicilioRepository;
    @Mock
    private GestoPagoCuentaRepository cuentaRepository;
    @Mock
    private GestoPagoUsuarioRepository usuarioRepository;
    @Mock
    private GestoPagoClienteMapper clienteMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GestoPagoClienteServiceImpl service;

    @Test
    void registraCliente() {
        GestoPagoClienteRegistroRequest request = alta();

        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setId(7);
        GestoPagoDomicilio domicilio = new GestoPagoDomicilio();

        when(passwordEncoder.encode("ClaveSegura1!")).thenReturn("hash-bcrypt");
        when(clienteMapper.toCliente(request)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toDomicilio(request.getDomicilio())).thenReturn(domicilio);
        when(cuentaRepository.generarNumeroCuenta()).thenReturn("00000000000000000007");

        var response = service.registrarCliente(request);

        ArgumentCaptor<GestoPagoCuenta> cuenta = ArgumentCaptor.forClass(GestoPagoCuenta.class);
        ArgumentCaptor<GestoPagoUsuario> usuario = ArgumentCaptor.forClass(GestoPagoUsuario.class);
        verify(domicilioRepository).save(domicilio);
        verify(cuentaRepository).save(cuenta.capture());
        verify(usuarioRepository).save(usuario.capture());

        assertSame(cliente, domicilio.getCliente());
        assertSame(cliente, cuenta.getValue().getCliente());
        assertEquals("00000000000000000007", cuenta.getValue().getNumeroCuenta());
        assertEquals("0.00", cuenta.getValue().getSaldo().toPlainString());
        assertEquals("ACTIVA", cuenta.getValue().getEstatus());
        assertSame(cliente, usuario.getValue().getCliente());
        assertEquals("cliente@example.com", usuario.getValue().getCorreo());
        assertEquals("hash-bcrypt", usuario.getValue().getPasswordHash());
        assertTrue(usuario.getValue().isActivo());
        assertEquals(7, response.getClienteId());
        assertEquals(cuenta.getValue().getNumeroCuenta(), response.getNumeroCuenta());
        assertEquals(cuenta.getValue().getSaldo(), response.getSaldoInicial());
    }

    @Test
    void rechazaContrasenaLarga() {
        GestoPagoClienteRegistroRequest request = new GestoPagoClienteRegistroRequest();
        request.setContrasena("Á".repeat(40));

        assertThrows(GestoPagoContrasenaInvalidaException.class,
                () -> service.registrarCliente(request));

        verifyNoInteractions(passwordEncoder, clienteRepository, usuarioRepository);
    }

    @Test
    void rechazaMenorDeEdad() {
        var request = alta();
        request.setFechaNacimiento(LocalDate.now().minusYears(18).plusDays(1));

        assertThrows(GestoPagoClienteInvalidoException.class,
                () -> service.registrarCliente(request));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void aceptaDieciochoAnios() {
        var request = alta();
        request.setFechaNacimiento(LocalDate.now().minusYears(18));
        var cliente = new GestoPagoCliente();
        cliente.setId(7);

        when(clienteMapper.toCliente(request)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toDomicilio(request.getDomicilio()))
                .thenReturn(new GestoPagoDomicilio());
        when(cuentaRepository.generarNumeroCuenta()).thenReturn("00000000000000000007");

        assertEquals(7, service.registrarCliente(request).getClienteId());
    }

    @Test
    void rechazaCurpRepetida() {
        var request = alta();
        when(clienteRepository.existsByCurp(request.getCurp())).thenReturn(true);

        assertThrows(GestoPagoClienteDuplicadoException.class,
                () -> service.registrarCliente(request));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void rechazaRfcRepetido() {
        var request = alta();
        when(clienteRepository.existsByRfc(request.getRfc())).thenReturn(true);

        assertThrows(GestoPagoClienteDuplicadoException.class,
                () -> service.registrarCliente(request));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void rechazaCorreoRepetido() {
        var request = alta();
        when(usuarioRepository.existsByCorreoIgnoreCase(request.getCorreo())).thenReturn(true);

        assertThrows(GestoPagoCorreoDuplicadoException.class,
                () -> service.registrarCliente(request));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void actualizaCorreo() {
        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setId(7);
        GestoPagoUsuario usuario = new GestoPagoUsuario();
        usuario.setCorreo("anterior@example.com");
        GestoPagoClienteParcialRequest request = new GestoPagoClienteParcialRequest();
        request.setCorreo("nuevo@example.com");

        when(clienteRepository.findById(7)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByCliente_Id(7)).thenReturn(Optional.of(usuario));

        service.actualizarClienteParcial(7, request);

        assertEquals("nuevo@example.com", usuario.getCorreo());
        verify(usuarioRepository).existsByCorreoIgnoreCase("nuevo@example.com");
    }

    @Test
    void rechazaCorreoOcupado() {
        GestoPagoCliente cliente = new GestoPagoCliente();
        GestoPagoUsuario usuario = new GestoPagoUsuario();
        usuario.setCorreo("anterior@example.com");
        GestoPagoClienteParcialRequest request = new GestoPagoClienteParcialRequest();
        request.setCorreo("ocupado@example.com");

        when(clienteRepository.findById(7)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByCliente_Id(7)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByCorreoIgnoreCase("ocupado@example.com"))
                .thenReturn(true);

        assertThrows(GestoPagoCorreoDuplicadoException.class,
                () -> service.actualizarClienteParcial(7, request));

        assertEquals("anterior@example.com", usuario.getCorreo());
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void actualizaDatos() {
        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setId(7);
        GestoPagoUsuario usuario = new GestoPagoUsuario();
        usuario.setCorreo("anterior@example.com");
        GestoPagoDomicilio domicilio = new GestoPagoDomicilio();
        GestoPagoClienteActualizacionRequest request =
                new GestoPagoClienteActualizacionRequest();
        request.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        request.setCorreo("nuevo@example.com");
        request.setDomicilio(new GestoPagoDomicilioRequest());

        when(clienteRepository.findById(7)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByCliente_Id(7)).thenReturn(Optional.of(usuario));
        when(domicilioRepository.findByCliente_Id(7)).thenReturn(Optional.of(domicilio));

        service.actualizarCliente(7, request);

        assertEquals("nuevo@example.com", usuario.getCorreo());
        verify(clienteMapper).actualizarCliente(request, cliente);
        verify(clienteMapper).actualizarDomicilio(request.getDomicilio(), domicilio);
    }

    @Test
    void desactivaCliente() {
        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setId(7);
        GestoPagoUsuario usuario = new GestoPagoUsuario();
        GestoPagoCuenta cuenta = new GestoPagoCuenta();

        when(clienteRepository.findById(7)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByCliente_Id(7)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_Id(7)).thenReturn(List.of(cuenta));

        service.desactivarCliente(7);

        assertFalse(cliente.isActiva());
        assertFalse(usuario.isActivo());
        assertEquals("INACTIVA", cuenta.getEstatus());
    }

    @Test
    void clienteInexistente() {
        assertThrows(GestoPagoClienteNoEncontradoException.class,
                () -> service.desactivarCliente(99));
        verifyNoInteractions(usuarioRepository, cuentaRepository);
    }

    private GestoPagoClienteRegistroRequest alta() {
        var request = new GestoPagoClienteRegistroRequest();
        request.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        request.setCurp("CANO050429HKNTEW00");
        request.setRfc("CANO0504295G3");
        request.setCorreo("cliente@example.com");
        request.setContrasena("ClaveSegura1!");
        request.setDomicilio(new GestoPagoDomicilioRequest());
        return request;
    }

}
