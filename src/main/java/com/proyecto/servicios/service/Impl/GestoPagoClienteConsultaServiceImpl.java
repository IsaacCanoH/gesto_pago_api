package com.proyecto.servicios.service.Impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoClienteInvalidoException;
import com.proyecto.servicios.exception.GestoPagoClienteNoEncontradoException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoEncontradaException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteResumenResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoClienteRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoDomicilioRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoClienteConsultaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "sfTransactionManager", readOnly = true)
public class GestoPagoClienteConsultaServiceImpl implements GestoPagoClienteConsultaService {
    private final GestoPagoClienteRepository clienteRepository;
    private final GestoPagoDomicilioRepository domicilioRepository;
    private final GestoPagoCuentaRepository cuentaRepository;
    private final GestoPagoUsuarioRepository usuarioRepository;
    private final GestoPagoClienteMapper clienteMapper;

    @Override
    public GestoPagoClienteConsultaResponse consultarCliente(Integer id) {
        GestoPagoCliente cliente = clienteRepository.findById(id)
                .orElseThrow(GestoPagoClienteNoEncontradoException::new);
        GestoPagoDomicilio domicilio = domicilioRepository.findByCliente_Id(id)
                .orElseThrow(() -> new IllegalStateException(
                        "El cliente no tiene domicilio registrado"));
        GestoPagoUsuario usuario = usuarioRepository.findByCliente_Id(id)
                .orElseThrow(() -> new IllegalStateException(
                        "El cliente no tiene usuario registrado"));

        return construirRespuesta(
                cliente, domicilio, cuentaRepository.findByCliente_Id(id), usuario);
    }

    @Override
    public GestoPagoClienteConsultaResponse consultarClientePorCurp(String curp) {
        GestoPagoCliente cliente = clienteRepository.findByCurp(curp)
                .orElseThrow(GestoPagoClienteNoEncontradoException::new);
        return consultarCliente(cliente.getId());
    }

    @Override
    public GestoPagoClienteConsultaResponse consultarClientePorRfc(String rfc) {
        GestoPagoCliente cliente = clienteRepository.findByRfc(rfc)
                .orElseThrow(GestoPagoClienteNoEncontradoException::new);
        return consultarCliente(cliente.getId());
    }

    @Override
    public GestoPagoClienteConsultaResponse consultarClientePorNumeroCuenta(String numeroCuenta) {
        GestoPagoCuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(GestoPagoCuentaNoEncontradaException::new);
        return consultarCliente(cuenta.getCliente().getId());
    }

    @Override
    public GestoPagoClienteConsultaResponse consultarClientePorCorreo(String correo) {
        GestoPagoUsuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(GestoPagoClienteNoEncontradoException::new);
        return consultarCliente(usuario.getCliente().getId());
    }

    @Override
    public Page<GestoPagoClienteResumenResponse> consultarClientes(int pagina, int tamanio) {
        return resumirPagina(clienteRepository.findAll(crearPageRequest(pagina, tamanio)));
    }

    @Override
    public Page<GestoPagoClienteResumenResponse> consultarClientesActivos(int pagina, int tamanio) {
        return resumirPagina(clienteRepository.findByActivaTrue(crearPageRequest(pagina, tamanio)));
    }

    @Override
    public Page<GestoPagoClienteResumenResponse> consultarClientesPorFechaCreacion(
            LocalDate desde, LocalDate hasta, int pagina, int tamanio) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new GestoPagoClienteInvalidoException(
                    "El rango de fechas no es válido: desde debe ser anterior o igual a hasta");
        }
        return resumirPagina(clienteRepository.findByFechaCreacionBetween(
                desde, hasta, crearPageRequest(pagina, tamanio)));
    }

    private PageRequest crearPageRequest(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100) {
            throw new GestoPagoClienteInvalidoException(
                    "La página debe ser mayor o igual a 0 y el tamaño debe estar entre 1 y 100");
        }
        return PageRequest.of(pagina, tamanio, Sort.by("id").ascending());
    }

    private Page<GestoPagoClienteResumenResponse> resumirPagina(Page<GestoPagoCliente> clientes) {
        List<Integer> ids = clientes.getContent().stream()
                .map(GestoPagoCliente::getId)
                .toList();

        Map<Integer, GestoPagoUsuario> usuarios = ids.isEmpty()
                ? Map.of()
                : usuarioRepository.findByCliente_IdIn(ids).stream()
                        .collect(Collectors.toMap(
                                usuario -> usuario.getCliente().getId(),
                                usuario -> usuario));

        return clientes.map(cliente -> {
            GestoPagoUsuario usuario = usuarios.get(cliente.getId());
            if (usuario == null) {
                throw new IllegalStateException("El cliente no tiene usuario registrado");
            }
            return clienteMapper.toResumenCliente(cliente, usuario.getCorreo());
        });
    }

    private GestoPagoClienteConsultaResponse construirRespuesta(
            GestoPagoCliente cliente,
            GestoPagoDomicilio domicilio,
            List<GestoPagoCuenta> cuentas,
            GestoPagoUsuario usuario) {
        GestoPagoClienteConsultaResponse response = clienteMapper.toConsultaCliente(cliente);
        response.setCorreo(usuario.getCorreo());
        response.setDomicilio(clienteMapper.toConsultaDomicilio(domicilio));
        response.setCuentas(cuentas.stream().map(clienteMapper::toConsultaCuenta).toList());
        return response;
    }
}
