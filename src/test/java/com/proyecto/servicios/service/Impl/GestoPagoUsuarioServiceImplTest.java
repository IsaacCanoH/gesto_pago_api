package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.proyecto.servicios.exception.GestoPagoAccesoDenegadoException;
import com.proyecto.servicios.exception.GestoPagoContrasenaInvalidaException;
import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;
import com.proyecto.servicios.exception.GestoPagoUsuarioInactivoException;
import com.proyecto.servicios.exception.GestoPagoUsuarioNoEncontradoException;
import com.proyecto.servicios.model.gestopago.GestoPagoCambioContrasenaRequest;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GestoPagoUsuarioServiceImplTest {
    @Mock private GestoPagoUsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private GestoPagoUsuarioServiceImpl service;

    private GestoPagoUsuario usuario;

    @BeforeEach
    void prepararUsuario() {
        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setId(3);
        cliente.setActiva(true);
        usuario = new GestoPagoUsuario();
        usuario.setId(7);
        usuario.setCliente(cliente);
        usuario.setCorreo("cliente@example.com");
        usuario.setPasswordHash("hash-viejo");
    }

    @Test
    void consultaUsuario() {
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));

        var response = service.consultarUsuario(7, 7);

        assertEquals(7, response.id());
        assertEquals(3, response.clienteId());
        assertEquals("cliente@example.com", response.correo());
    }

    @Test
    void otroUsuarioNoConsulta() {
        assertThrows(GestoPagoAccesoDenegadoException.class,
                () -> service.consultarUsuario(7, 8));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void cambiaContrasena() {
        var request = new GestoPagoCambioContrasenaRequest("ClaveVieja1!", "ClaveNueva2!");
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.contrasenaActual(), "hash-viejo")).thenReturn(true);
        when(passwordEncoder.encode(request.contrasenaNueva())).thenReturn("hash-nuevo");

        service.cambiarContrasena(7, 7, request);

        assertEquals("hash-nuevo", usuario.getPasswordHash());
        verify(passwordEncoder).encode("ClaveNueva2!");
    }

    @Test
    void contrasenaActualIncorrecta() {
        var request = new GestoPagoCambioContrasenaRequest("incorrecta", "ClaveNueva2!");
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));

        assertThrows(GestoPagoCredencialesInvalidasException.class,
                () -> service.cambiarContrasena(7, 7, request));
        assertEquals("hash-viejo", usuario.getPasswordHash());
    }

    @Test
    void contrasenaDebil() {
        var request = new GestoPagoCambioContrasenaRequest("ClaveVieja1!", "debil");
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.contrasenaActual(), "hash-viejo")).thenReturn(true);

        assertThrows(GestoPagoContrasenaInvalidaException.class,
                () -> service.cambiarContrasena(7, 7, request));
        assertEquals("hash-viejo", usuario.getPasswordHash());
    }

    @Test
    void usuarioInexistente() {
        when(usuarioRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(GestoPagoUsuarioNoEncontradoException.class,
                () -> service.consultarUsuario(7, 7));
    }

    @Test
    void usuarioInactivo() {
        usuario.setActivo(false);
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));

        assertThrows(GestoPagoUsuarioInactivoException.class,
                () -> service.consultarUsuario(7, 7));
    }

    @Test
    void otroUsuarioNoCambiaLaContrasena() {
        var request = new GestoPagoCambioContrasenaRequest("ClaveVieja1!", "ClaveNueva2!");

        assertThrows(GestoPagoAccesoDenegadoException.class,
                () -> service.cambiarContrasena(7, 8, request));
        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    void noReutilizaContrasena() {
        var request = new GestoPagoCambioContrasenaRequest("ClaveVieja1!", "ClaveVieja1!");
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.contrasenaActual(), "hash-viejo"))
                .thenReturn(true);

        assertThrows(GestoPagoContrasenaInvalidaException.class,
                () -> service.cambiarContrasena(7, 7, request));
        assertEquals("hash-viejo", usuario.getPasswordHash());
    }
}
