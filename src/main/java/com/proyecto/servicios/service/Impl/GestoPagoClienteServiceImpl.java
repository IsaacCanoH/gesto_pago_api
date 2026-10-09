package com.proyecto.servicios.service.Impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoClienteDuplicadoException;
import com.proyecto.servicios.exception.GestoPagoClienteInvalidoException;
import com.proyecto.servicios.exception.GestoPagoClienteNoEncontradoException;
import com.proyecto.servicios.exception.GestoPagoCorreoDuplicadoException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteActualizacionRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteParcialRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoClienteRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoDomicilioRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoClienteService;
import com.proyecto.servicios.service.GestoPagoContrasenaPolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestoPagoClienteServiceImpl implements GestoPagoClienteService {
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("0.00");

    private final GestoPagoClienteRepository clienteRepository;
    private final GestoPagoDomicilioRepository domicilioRepository;
    private final GestoPagoCuentaRepository cuentaRepository;
    private final GestoPagoUsuarioRepository usuarioRepository;
    private final GestoPagoClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public GestoPagoClienteRegistroResponse registrarCliente(GestoPagoClienteRegistroRequest request) {
        GestoPagoContrasenaPolicy.validar(request.getContrasena());
        String passwordHash = passwordEncoder.encode(request.getContrasena());
        validarRegistro(request);

        GestoPagoCliente cliente = clienteRepository.save(clienteMapper.toCliente(request));

        GestoPagoDomicilio domicilio = clienteMapper.toDomicilio(request.getDomicilio());
        domicilio.setCliente(cliente);
        domicilioRepository.save(domicilio);

        GestoPagoCuenta cuenta = new GestoPagoCuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta(cuentaRepository.generarNumeroCuenta());
        cuenta.setSaldo(SALDO_INICIAL);
        cuentaRepository.save(cuenta);

        GestoPagoUsuario usuario = new GestoPagoUsuario();
        usuario.setCliente(cliente);
        usuario.setCorreo(request.getCorreo());
        usuario.setPasswordHash(passwordHash);
        usuarioRepository.save(usuario);

        return new GestoPagoClienteRegistroResponse(
                cliente.getId(), cuenta.getNumeroCuenta(), cuenta.getSaldo());
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void actualizarCliente(Integer id, GestoPagoClienteActualizacionRequest request) {
        GestoPagoCliente cliente = obtenerCliente(id);
        GestoPagoUsuario usuario = obtenerUsuario(id);
        GestoPagoDomicilio domicilio = obtenerDomicilio(id);

        validarMayoriaEdad(request.getFechaNacimiento());
        actualizarCorreo(usuario, request.getCorreo());
        clienteMapper.actualizarCliente(request, cliente);
        clienteMapper.actualizarDomicilio(request.getDomicilio(), domicilio);
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void actualizarClienteParcial(Integer id, GestoPagoClienteParcialRequest request) {
        GestoPagoCliente cliente = obtenerCliente(id);

        if (request.getFechaNacimiento() != null) {
            validarMayoriaEdad(request.getFechaNacimiento());
        }
        if (request.getCorreo() != null) {
            actualizarCorreo(obtenerUsuario(id), request.getCorreo());
        }
        if (request.getDomicilio() != null) {
            clienteMapper.actualizarDomicilioParcial(
                    request.getDomicilio(), obtenerDomicilio(id));
        }

        clienteMapper.actualizarClienteParcial(request, cliente);
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void desactivarCliente(Integer id) {
        GestoPagoCliente cliente = obtenerCliente(id);
        GestoPagoUsuario usuario = obtenerUsuario(id);

        cliente.setActiva(false);
        usuario.setActivo(false);
        cuentaRepository.findByCliente_Id(id)
                .forEach(cuenta -> cuenta.setEstatus("INACTIVA"));
    }

    private GestoPagoCliente obtenerCliente(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(GestoPagoClienteNoEncontradoException::new);
    }

    private GestoPagoDomicilio obtenerDomicilio(Integer clienteId) {
        return domicilioRepository.findByCliente_Id(clienteId)
                .orElseThrow(() -> new IllegalStateException(
                        "El cliente no tiene domicilio registrado"));
    }

    private GestoPagoUsuario obtenerUsuario(Integer clienteId) {
        return usuarioRepository.findByCliente_Id(clienteId)
                .orElseThrow(() -> new IllegalStateException(
                        "El cliente no tiene usuario registrado"));
    }

    private void actualizarCorreo(GestoPagoUsuario usuario, String correo) {
        if (!usuario.getCorreo().equalsIgnoreCase(correo)
                && usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new GestoPagoCorreoDuplicadoException();
        }
        usuario.setCorreo(correo);
    }

    private void validarRegistro(GestoPagoClienteRegistroRequest request) {
        validarMayoriaEdad(request.getFechaNacimiento());

        if (clienteRepository.existsByCurp(request.getCurp())) {
            throw new GestoPagoClienteDuplicadoException("La CURP ya está registrada");
        }
        if (clienteRepository.existsByRfc(request.getRfc())) {
            throw new GestoPagoClienteDuplicadoException("El RFC ya está registrado");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(request.getCorreo())) {
            throw new GestoPagoCorreoDuplicadoException();
        }
    }

    private void validarMayoriaEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento.isAfter(LocalDate.now().minusYears(18))) {
            throw new GestoPagoClienteInvalidoException("El cliente debe ser mayor de edad");
        }
    }
}
