package com.proyecto.servicios.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoClienteInvalidoException;
import com.proyecto.servicios.exception.GestoPagoClienteNoEncontradoException;
import com.proyecto.servicios.mapper.GestoPagoClienteMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteBusquedaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteResumenResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoClienteRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoDomicilioRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoClienteConsultaService;
import com.proyecto.servicios.service.GestoPagoClienteBusquedaResultado;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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
    public GestoPagoClienteBusquedaResultado buscarClientes(GestoPagoClienteBusquedaRequest request) {
        if (request.getDesde() != null && request.getHasta() != null
                && request.getDesde().isAfter(request.getHasta())) {
            throw new GestoPagoClienteInvalidoException(
                    "El rango de fechas no es válido: desde debe ser anterior o igual a hasta");
        }
        if (request.isDetalle() && request.getPagina() != 0) {
            throw new GestoPagoClienteInvalidoException(
                    "Para consultar el detalle, la página debe ser cero");
        }

        Page<GestoPagoClienteResumenResponse> resultados = resumirPagina(
                clienteRepository.findAll(crearFiltro(request),
                        crearPageRequest(request.getPagina(), request.getTamanio())));
        GestoPagoClienteConsultaResponse detalle = null;
        if (request.isDetalle()) {
            if (resultados.getTotalElements() == 0) {
                throw new GestoPagoClienteNoEncontradoException();
            }
            if (resultados.getTotalElements() != 1) {
                throw new GestoPagoClienteInvalidoException(
                        "Para consultar el detalle, los filtros deben identificar un solo cliente");
            }
            detalle = consultarCliente(resultados.getContent().get(0).id());
        }
        return new GestoPagoClienteBusquedaResultado(resultados, detalle);
    }

    private Specification<GestoPagoCliente> crearFiltro(GestoPagoClienteBusquedaRequest request) {
        return (cliente, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (request.getId() != null) {
                filtros.add(cb.equal(cliente.get("id"), request.getId()));
            }
            if (request.getCurp() != null) {
                String curp = request.getCurp().toUpperCase(Locale.ROOT);
                filtros.add(request.isParcial()
                        ? cb.like(cb.upper(cliente.get("curp")), "%" + curp + "%")
                        : cb.equal(cb.upper(cliente.get("curp")), curp));
            }
            if (request.getRfc() != null) {
                String rfc = request.getRfc().toUpperCase(Locale.ROOT);
                filtros.add(request.isParcial()
                        ? cb.like(cb.upper(cliente.get("rfc")), "%" + rfc + "%")
                        : cb.equal(cb.upper(cliente.get("rfc")), rfc));
            }
            if (request.getCorreo() != null) {
                Subquery<Integer> usuarios = query.subquery(Integer.class);
                Root<GestoPagoUsuario> usuario = usuarios.from(GestoPagoUsuario.class);
                usuarios.select(usuario.get("cliente").get("id"))
                        .where(cb.equal(cb.lower(usuario.get("correo")),
                                request.getCorreo().toLowerCase(Locale.ROOT)));
                filtros.add(cliente.get("id").in(usuarios));
            }
            if (request.getNumeroCuenta() != null) {
                Subquery<Integer> cuentas = query.subquery(Integer.class);
                Root<GestoPagoCuenta> cuenta = cuentas.from(GestoPagoCuenta.class);
                cuentas.select(cuenta.get("cliente").get("id"))
                        .where(cb.equal(cuenta.get("numeroCuenta"), request.getNumeroCuenta()));
                filtros.add(cliente.get("id").in(cuentas));
            }
            if (request.getActiva() != null) {
                filtros.add(cb.equal(cliente.get("activa"), request.getActiva()));
            }
            if (request.getDesde() != null) {
                filtros.add(cb.greaterThanOrEqualTo(cliente.get("fechaCreacion"), request.getDesde()));
            }
            if (request.getHasta() != null) {
                filtros.add(cb.lessThanOrEqualTo(cliente.get("fechaCreacion"), request.getHasta()));
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        };
    }

    private GestoPagoClienteConsultaResponse consultarCliente(Integer id) {
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
