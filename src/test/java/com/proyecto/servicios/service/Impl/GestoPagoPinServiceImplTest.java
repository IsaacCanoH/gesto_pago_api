package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoPinBloqueadoException;
import com.proyecto.servicios.exception.GestoPagoPinInvalidoException;
import com.proyecto.servicios.exception.GestoPagoPinNoConfiguradoException;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GestoPagoPinServiceImplTest {
    @Mock private GestoPagoUsuarioRepository usuarios;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private GestoPagoPinServiceImpl service;
    private GestoPagoUsuario usuario;

    @BeforeEach
    void preparar() {
        service = new GestoPagoPinServiceImpl(usuarios, encoder);
        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setId(3);
        usuario = new GestoPagoUsuario();
        usuario.setCliente(cliente);
        when(usuarios.findByIdParaPin(7)).thenReturn(Optional.of(usuario));
    }

    @Test
    void pinCorrectoAutorizaYLimpiaIntentos() {
        usuario.setPinHash(encoder.encode("123456"));
        usuario.setPinIntentosFallidos(2);

        service.validar(7, "123456");

        assertEquals(0, usuario.getPinIntentosFallidos());
    }

    @Test
    void quintoIntentoBloqueaTemporalmente() {
        usuario.setPinHash(encoder.encode("123456"));
        usuario.setPinIntentosFallidos(4);

        assertThrows(GestoPagoPinBloqueadoException.class,
                () -> service.validar(7, "000000"));

        assertEquals(0, usuario.getPinIntentosFallidos());
        assertEquals(true, usuario.getPinBloqueadoHasta().isAfter(Instant.now()));
        assertThrows(GestoPagoPinBloqueadoException.class,
                () -> service.validar(7, "123456"));
    }

    @Test
    void pinIncorrectoNoAutoriza() {
        usuario.setPinHash(encoder.encode("123456"));

        assertThrows(GestoPagoPinInvalidoException.class,
                () -> service.validar(7, "000000"));

        assertEquals(1, usuario.getPinIntentosFallidos());
    }

    @Test
    void clienteAnteriorSinPinNoAutoriza() {
        assertThrows(GestoPagoPinNoConfiguradoException.class,
                () -> service.validar(7, "123456"));
    }
}
