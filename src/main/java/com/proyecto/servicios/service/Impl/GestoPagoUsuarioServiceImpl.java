package com.proyecto.servicios.service.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoAccesoDenegadoException;
import com.proyecto.servicios.exception.GestoPagoContrasenaInvalidaException;
import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;
import com.proyecto.servicios.exception.GestoPagoUsuarioInactivoException;
import com.proyecto.servicios.exception.GestoPagoUsuarioNoEncontradoException;
import com.proyecto.servicios.model.gestopago.GestoPagoCambioContrasenaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoUsuarioConsultaResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoContrasenaPolicy;
import com.proyecto.servicios.service.GestoPagoUsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestoPagoUsuarioServiceImpl implements GestoPagoUsuarioService {
    private final GestoPagoUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(transactionManager = "sfTransactionManager", readOnly = true)
    public GestoPagoUsuarioConsultaResponse consultarUsuario(Integer id, Integer solicitanteId) {
        verificarPropietario(id, solicitanteId);
        GestoPagoUsuario usuario = obtenerUsuarioActivo(id);
        return new GestoPagoUsuarioConsultaResponse(
                usuario.getId(), usuario.getCliente().getId(), usuario.getCorreo(),
                usuario.isActivo(), usuario.getFechaCreacion(), usuario.getFechaActualizacion());
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void cambiarContrasena(
            Integer id, Integer solicitanteId, GestoPagoCambioContrasenaRequest request) {
        verificarPropietario(id, solicitanteId);
        GestoPagoUsuario usuario = obtenerUsuarioActivo(id);

        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getPasswordHash())) {
            throw new GestoPagoCredencialesInvalidasException();
        }
        GestoPagoContrasenaPolicy.validar(request.contrasenaNueva());
        if (request.contrasenaNueva().equals(request.contrasenaActual())) {
            throw new GestoPagoContrasenaInvalidaException(
                    "La contraseña nueva debe ser diferente de la actual");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.contrasenaNueva()));
    }

    private GestoPagoUsuario obtenerUsuarioActivo(Integer id) {
        GestoPagoUsuario usuario = usuarioRepository.findById(id)
                .orElseThrow(GestoPagoUsuarioNoEncontradoException::new);
        if (!usuario.isActivo() || !usuario.getCliente().isActiva()) {
            throw new GestoPagoUsuarioInactivoException();
        }
        return usuario;
    }

    private void verificarPropietario(Integer id, Integer solicitanteId) {
        if (solicitanteId != null && !id.equals(solicitanteId)) {
            throw new GestoPagoAccesoDenegadoException();
        }
    }
}
