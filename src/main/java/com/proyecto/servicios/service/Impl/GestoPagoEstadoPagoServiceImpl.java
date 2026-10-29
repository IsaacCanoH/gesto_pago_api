package com.proyecto.servicios.service.Impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoPagoInvalidoException;
import com.proyecto.servicios.exception.GestoPagoPagoNoEncontradoException;
import com.proyecto.servicios.exception.GestoPagoSolicitudDuplicadaException;
import com.proyecto.servicios.exception.GestoPagoUsuarioNoEncontradoException;
import com.proyecto.servicios.model.gestopago.GestoPagoFormularioTx;
import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoPagoRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoEstadoPagoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestoPagoEstadoPagoServiceImpl implements GestoPagoEstadoPagoService {
    private static final Duration ESPERA_CONFIRMACION = Duration.ofSeconds(90);
    private static final Duration VIGENCIA_CONFIRMACION = Duration.ofHours(24);
    private static final Duration INTERVALO_CONFIRMACION = Duration.ofMinutes(1);

    private final GestoPagoPagoRepository pagoRepository;
    private final GestoPagoCuentaRepository cuentaRepository;
    private final GestoPagoUsuarioRepository usuarioRepository;

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public GestoPagoPago iniciarEnvio(Long pagoId, GestoPagoFormularioTx formulario) {
        GestoPagoPago pago = buscarBloqueado(pagoId);
        if (pago.getEstatus() != GestoPagoEstadoPago.PENDIENTE || pago.getFechaEnvio() != null) {
            throw new GestoPagoPagoInvalidoException("Este pago ya fue enviado o resuelto");
        }
        if (formulario == null || formulario.random() == null || formulario.random().isBlank()
                || formulario.signed() == null || formulario.signed().isBlank()) {
            throw new GestoPagoPagoInvalidoException("La solicitud del proveedor está incompleta");
        }
        pago.setRandomProveedor(formulario.random());
        pago.setSignedProveedor(formulario.signed());
        pago.setFechaEnvio(Instant.now());
        return pago;
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void cancelarSinEnvio(Long pagoId) {
        GestoPagoPago pago = buscarBloqueado(pagoId);
        if (pago.getEstatus() == GestoPagoEstadoPago.PENDIENTE && pago.getFechaEnvio() == null) {
            resolver(pago, GestoPagoEstadoPago.FALLIDO);
        }
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void registrarEnvio(Long pagoId, GestoPagoRespuestaEnvio respuesta) {
        GestoPagoPago pago = buscarBloqueado(pagoId);
        if (pago.getEstatus() != GestoPagoEstadoPago.PENDIENTE) {
            return;
        }
        guardarRespuesta(pago, respuesta.codigo(), respuesta.mensaje(),
                respuesta.idTransaccion(), respuesta.numeroAutorizacion());
        if (respuesta.exitosa()) {
            resolver(pago, GestoPagoEstadoPago.EXITOSO);
        } else if (respuesta.rechazada()) {
            resolver(pago, GestoPagoEstadoPago.FALLIDO);
        }
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public Optional<GestoPagoPago> iniciarConfirmacion(Long pagoId) {
        GestoPagoPago pago = buscarBloqueado(pagoId);
        Instant ahora = Instant.now();
        Instant envio = pago.getFechaEnvio();
        if (pago.getEstatus() != GestoPagoEstadoPago.PENDIENTE || envio == null
                || envio.isAfter(ahora.minus(ESPERA_CONFIRMACION))
                || envio.isBefore(ahora.minus(VIGENCIA_CONFIRMACION))
                || (pago.getFechaUltimaConfirmacion() != null
                        && pago.getFechaUltimaConfirmacion().isAfter(ahora.minus(INTERVALO_CONFIRMACION)))) {
            return Optional.empty();
        }
        pago.setFechaUltimaConfirmacion(ahora);
        return Optional.of(pago);
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public void registrarConfirmacion(Long pagoId, GestoPagoRespuestaConfirmacion respuesta) {
        GestoPagoPago pago = buscarBloqueado(pagoId);
        if (pago.getEstatus() != GestoPagoEstadoPago.PENDIENTE) {
            return;
        }
        guardarRespuesta(pago, respuesta.codigo(), respuesta.mensaje(),
                respuesta.idTransaccion(), respuesta.numeroAutorizacion());
        if (respuesta.aplicada()) {
            resolver(pago, GestoPagoEstadoPago.EXITOSO);
        } else if (respuesta.noAplicada()) {
            resolver(pago, GestoPagoEstadoPago.FALLIDO);
        }
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager", readOnly = true)
    public GestoPagoPagoConsultaResponse consultar(String upc, Integer usuarioId) {
        GestoPagoUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(GestoPagoUsuarioNoEncontradoException::new);
        GestoPagoPago pago = pagoRepository.findByUpc(upc)
                .filter(encontrado -> encontrado.getCuenta().getCliente().getId()
                        .equals(usuario.getCliente().getId()))
                .orElseThrow(GestoPagoPagoNoEncontradoException::new);
        return GestoPagoPagoConsultaResponse.desde(pago);
    }

    @Override
    @Transactional(transactionManager = "sfTransactionManager", readOnly = true)
    public Optional<GestoPagoPagoConsultaResponse> buscarSolicitud(
            UUID solicitudId, GestoPagoReservaPagoCommand command, Integer usuarioId) {
        if (solicitudId == null) {
            return Optional.empty();
        }
        return pagoRepository.findBySolicitudId(solicitudId).map(pago -> {
            GestoPagoUsuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(GestoPagoUsuarioNoEncontradoException::new);
            boolean pertenece = pago.getCuenta().getCliente().getId()
                    .equals(usuario.getCliente().getId());
            boolean mismaSolicitud = Objects.equals(pago.getCuenta().getNumeroCuenta(), command.numeroCuenta())
                    && Objects.equals(pago.getIdServicioProveedor(), command.idServicio())
                    && Objects.equals(pago.getIdProductoProveedor(), command.idProducto())
                    && Objects.equals(pago.getProducto().getTipoFront(), command.tipoFront())
                    && command.montoPago() != null
                    && pago.getMontoPago().compareTo(command.montoPago()) == 0
                    && Objects.equals(pago.getReferencia(), command.referencia())
                    && (pago.getReferencia() != null
                            || Objects.equals(pago.getTelefono(), command.telefono()));
            if (!pertenece || !mismaSolicitud) {
                throw new GestoPagoSolicitudDuplicadaException();
            }
            return GestoPagoPagoConsultaResponse.desde(pago);
        });
    }

    private GestoPagoPago buscarBloqueado(Long pagoId) {
        return pagoRepository.findByIdParaActualizar(pagoId)
                .orElseThrow(GestoPagoPagoNoEncontradoException::new);
    }

    private void resolver(GestoPagoPago pago, GestoPagoEstadoPago resultado) {
        GestoPagoCuenta cuenta = cuentaRepository.findByIdParaLiquidar(pago.getCuenta().getId())
                .orElseThrow(() -> new IllegalStateException("La cuenta del pago no existe"));
        BigDecimal monto = pago.getMontoPago();
        if (cuenta.getSaldoRetenido().compareTo(monto) < 0) {
            throw new IllegalStateException("La retención del pago es menor que su importe");
        }
        cuenta.setSaldoRetenido(cuenta.getSaldoRetenido().subtract(monto));
        if (resultado == GestoPagoEstadoPago.EXITOSO) {
            if (cuenta.getSaldo().compareTo(monto) < 0) {
                throw new IllegalStateException("El saldo de la cuenta es menor que el pago confirmado");
            }
            cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        }
        pago.setEstatus(resultado);
        pago.setFechaResolucion(Instant.now());
    }

    private void guardarRespuesta(GestoPagoPago pago, String codigo, String mensaje,
            String idTransaccion, String autorizacion) {
        pago.setCodigoProveedor(limitar(codigo, 10));
        pago.setMensajeProveedor(limitar(mensaje, 500));
        pago.setIdTransaccionProveedor(limitar(idTransaccion, 60));
        pago.setNumeroAutorizacion(limitar(autorizacion, 60));
    }

    private String limitar(String valor, int maximo) {
        return valor == null || valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }
}
