package com.proyecto.servicios.service.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;
import com.proyecto.servicios.exception.GestoPagoUsuarioInactivoException;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.security.GestoPagoJwtService;
import com.proyecto.servicios.security.GestoPagoPrincipal;
import com.proyecto.servicios.service.GestoPagoAuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestoPagoAuthServiceImpl implements GestoPagoAuthService {
    private final GestoPagoUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GestoPagoJwtService jwtService;

    @Override
    @Transactional(transactionManager = "sfTransactionManager", readOnly = true)
    public GestoPagoLoginResponse iniciarSesion(GestoPagoLoginRequest request) {
        GestoPagoUsuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.correo())
                .orElseThrow(GestoPagoCredencialesInvalidasException::new);

        if (!passwordEncoder.matches(request.contrasena(), usuario.getPasswordHash())) {
            throw new GestoPagoCredencialesInvalidasException();
        }
        validarActivo(usuario);

        return jwtService.generar(usuario.getId());
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager", readOnly = true)
    public GestoPagoPrincipal obtenerPrincipalActivo(Integer usuarioId) {
        GestoPagoUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(GestoPagoCredencialesInvalidasException::new);
        validarActivo(usuario);
        return new GestoPagoPrincipal(usuario.getId(), usuario.getCorreo());
    }

    private void validarActivo(GestoPagoUsuario usuario) {
        if (!usuario.isActivo() || !usuario.getCliente().isActiva()) {
            throw new GestoPagoUsuarioInactivoException();
        }
    }
}
