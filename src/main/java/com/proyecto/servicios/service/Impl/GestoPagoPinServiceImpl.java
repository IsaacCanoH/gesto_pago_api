package com.proyecto.servicios.service.Impl;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoPinBloqueadoException;
import com.proyecto.servicios.exception.GestoPagoPinInvalidoException;
import com.proyecto.servicios.exception.GestoPagoPinNoConfiguradoException;
import com.proyecto.servicios.exception.GestoPagoUsuarioInactivoException;
import com.proyecto.servicios.exception.GestoPagoUsuarioNoEncontradoException;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoPinService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestoPagoPinServiceImpl implements GestoPagoPinService {
    private static final int INTENTOS_MAXIMOS = 5;
    private static final Duration BLOQUEO = Duration.ofMinutes(15);

    private final GestoPagoUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(transactionManager = "sfTransactionManager",
            noRollbackFor = {GestoPagoPinInvalidoException.class, GestoPagoPinBloqueadoException.class})
    public void validar(Integer usuarioId, String pin) {
        GestoPagoUsuario usuario = obtenerUsuarioActivoParaPin(usuarioId);
        if (usuario.getPinHash() == null) {
            throw new GestoPagoPinNoConfiguradoException();
        }
        Instant ahora = Instant.now();
        if (usuario.getPinBloqueadoHasta() != null && ahora.isBefore(usuario.getPinBloqueadoHasta())) {
            throw new GestoPagoPinBloqueadoException();
        }
        if (!passwordEncoder.matches(pin, usuario.getPinHash())) {
            int intentos = usuario.getPinIntentosFallidos() + 1;
            if (intentos >= INTENTOS_MAXIMOS) {
                usuario.setPinIntentosFallidos(0);
                usuario.setPinBloqueadoHasta(ahora.plus(BLOQUEO));
                throw new GestoPagoPinBloqueadoException();
            }
            usuario.setPinIntentosFallidos(intentos);
            throw new GestoPagoPinInvalidoException();
        }
        usuario.setPinIntentosFallidos(0);
        usuario.setPinBloqueadoHasta(null);
    }

    private GestoPagoUsuario obtenerUsuarioActivoParaPin(Integer usuarioId) {
        GestoPagoUsuario usuario = usuarioRepository.findByIdParaPin(usuarioId)
                .orElseThrow(GestoPagoUsuarioNoEncontradoException::new);
        if (!usuario.isActivo() || !usuario.getCliente().isActiva()) {
            throw new GestoPagoUsuarioInactivoException();
        }
        return usuario;
    }
}
