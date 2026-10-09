package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;
import com.proyecto.servicios.exception.GestoPagoUsuarioInactivoException;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.security.GestoPagoJwtService;

@ExtendWith(MockitoExtension.class)
class GestoPagoAuthServiceImplTest {
    @Mock private GestoPagoUsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GestoPagoJwtService jwtService;
    @InjectMocks private GestoPagoAuthServiceImpl service;

    private GestoPagoUsuario usuario;

    @BeforeEach
    void prepararUsuario() {
        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setActiva(true);
        usuario = new GestoPagoUsuario();
        usuario.setId(7);
        usuario.setCliente(cliente);
        usuario.setCorreo("cliente@example.com");
        usuario.setPasswordHash("hash");
    }

    @Test
    void iniciaSesion() {
        var request = new GestoPagoLoginRequest("cliente@example.com", "ClaveSegura1!");
        var response = new GestoPagoLoginResponse("jwt", Instant.now());
        when(usuarioRepository.findByCorreoIgnoreCase(request.correo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.contrasena(), "hash")).thenReturn(true);
        when(jwtService.generar(7)).thenReturn(response);

        assertEquals(response, service.iniciarSesion(request));
    }

    @Test
    void correoDesconocido() {
        var request = new GestoPagoLoginRequest("otro@example.com", "ClaveSegura1!");
        when(usuarioRepository.findByCorreoIgnoreCase(request.correo())).thenReturn(Optional.empty());

        assertThrows(GestoPagoCredencialesInvalidasException.class,
                () -> service.iniciarSesion(request));
        verifyNoInteractions(jwtService);
    }

    @Test
    void contrasenaIncorrecta() {
        var request = new GestoPagoLoginRequest("cliente@example.com", "incorrecta");
        when(usuarioRepository.findByCorreoIgnoreCase(request.correo())).thenReturn(Optional.of(usuario));

        assertThrows(GestoPagoCredencialesInvalidasException.class,
                () -> service.iniciarSesion(request));
        verifyNoInteractions(jwtService);
    }

    @Test
    void usuarioInactivo() {
        usuario.setActivo(false);
        var request = new GestoPagoLoginRequest("cliente@example.com", "ClaveSegura1!");
        when(usuarioRepository.findByCorreoIgnoreCase(request.correo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.contrasena(), "hash")).thenReturn(true);

        assertThrows(GestoPagoUsuarioInactivoException.class,
                () -> service.iniciarSesion(request));
        verifyNoInteractions(jwtService);
    }

    @Test
    void clienteInactivo() {
        usuario.getCliente().setActiva(false);
        var request = new GestoPagoLoginRequest("cliente@example.com", "ClaveSegura1!");
        when(usuarioRepository.findByCorreoIgnoreCase(request.correo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.contrasena(), "hash")).thenReturn(true);

        assertThrows(GestoPagoUsuarioInactivoException.class,
                () -> service.iniciarSesion(request));
    }

    @Test
    void tokenDeUsuarioInactivo() {
        usuario.setActivo(false);
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));

        assertThrows(GestoPagoUsuarioInactivoException.class,
                () -> service.obtenerPrincipalActivo(7));
    }

    @Test
    void tokenDeUsuarioActivo() {
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));

        var principal = service.obtenerPrincipalActivo(7);

        assertEquals(7, principal.usuarioId());
        assertEquals("cliente@example.com", principal.correo());
    }

    @Test
    void tokenDeUsuarioInexistente() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(GestoPagoCredencialesInvalidasException.class,
                () -> service.obtenerPrincipalActivo(99));
    }
}
