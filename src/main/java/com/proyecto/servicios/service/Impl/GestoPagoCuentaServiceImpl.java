package com.proyecto.servicios.service.Impl;

import java.math.BigDecimal;
import java.util.ArrayList;

import jakarta.persistence.criteria.Predicate;

import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoAccesoDenegadoException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoCancelableException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoEncontradaException;
import com.proyecto.servicios.exception.GestoPagoUsuarioNoEncontradoException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaBusquedaRequest;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoCuentaService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GestoPagoCuentaServiceImpl implements GestoPagoCuentaService {

    private final GestoPagoCuentaRepository cuentaRepository;
    private final GestoPagoUsuarioRepository usuarioRepository;
    private final GestoPagoClienteMapper clienteMapper;

    @Override
    @Transactional(transactionManager = "sfTransactionManager", readOnly = true)
    public Page<GestoPagoCuentaConsultaResponse> buscarCuentas(GestoPagoCuentaBusquedaRequest request) {
        Specification<GestoPagoCuenta> filtro = (cuenta, query, cb) -> {
            var condiciones = new ArrayList<Predicate>();
            if (request.getNumeroCuenta() != null) {
                condiciones.add(cb.equal(cuenta.get("numeroCuenta"), request.getNumeroCuenta()));
            }
            if (Boolean.TRUE.equals(request.getSoloActivas())) {
                condiciones.add(cb.equal(cuenta.get("estatus"), "ACTIVA"));
                condiciones.add(cb.isTrue(cuenta.get("cliente").get("activa")));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        Page<GestoPagoCuentaConsultaResponse> resultados = cuentaRepository.findAll(filtro, PageRequest.of(
                        request.getPagina(), request.getTamanio(), Sort.by("id").ascending()))
                .map(clienteMapper::toConsultaCuenta);
        if (request.getNumeroCuenta() != null && resultados.getTotalElements() == 0) {
            throw new GestoPagoCuentaNoEncontradaException();
        }
        return resultados;
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void cancelarCuenta(String numeroCuenta, Integer usuarioId) {
        GestoPagoCuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(GestoPagoCuentaNoEncontradaException::new);
        if (usuarioId != null) {
            GestoPagoUsuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(GestoPagoUsuarioNoEncontradoException::new);
            if (!cuenta.getCliente().getId().equals(usuario.getCliente().getId())) {
                throw new GestoPagoAccesoDenegadoException("No puedes cancelar una cuenta de otro cliente");
            }
        }
        if ("INACTIVA".equals(cuenta.getEstatus())) {
            return;
        }
        if (cuenta.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new GestoPagoCuentaNoCancelableException();
        }

        cuenta.setEstatus("INACTIVA");
    }
}
