package com.proyecto.servicios.service.Impl;

import java.math.BigDecimal;

import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoAccesoDenegadoException;
import com.proyecto.servicios.exception.GestoPagoClienteInvalidoException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoCancelableException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoEncontradaException;
import com.proyecto.servicios.exception.GestoPagoUsuarioNoEncontradoException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoCuentaService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
    public GestoPagoCuentaConsultaResponse consultarCuenta(String numeroCuenta) {
        GestoPagoCuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(GestoPagoCuentaNoEncontradaException::new);

        return clienteMapper.toConsultaCuenta(cuenta);
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager", readOnly = true)
    public Page<GestoPagoCuentaConsultaResponse> consultarCuentasActivas(
            int pagina, int tamanio) {

        if (pagina < 0 || tamanio < 1 || tamanio > 100) {
            throw new GestoPagoClienteInvalidoException(
                    "La página debe ser mayor o igual a 0 y el tamaño debe estar entre 1 y 100");
        }

        return cuentaRepository.findByEstatusAndCliente_ActivaTrue(
                "ACTIVA",
                PageRequest.of(pagina, tamanio, Sort.by("id").ascending()))
                .map(clienteMapper::toConsultaCuenta);
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void cancelarCuenta(String numeroCuenta, Integer usuarioId) {
        GestoPagoCuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(GestoPagoCuentaNoEncontradaException::new);
        GestoPagoUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(GestoPagoUsuarioNoEncontradoException::new);

        if (!cuenta.getCliente().getId().equals(usuario.getCliente().getId())) {
            throw new GestoPagoAccesoDenegadoException("No puedes cancelar una cuenta de otro cliente");
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
