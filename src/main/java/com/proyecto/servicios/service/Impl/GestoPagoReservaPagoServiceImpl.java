package com.proyecto.servicios.service.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.config.GestoPagoPagoProperties;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoAccesoDenegadoException;
import com.proyecto.servicios.exception.GestoPagoCuentaInactivaException;
import com.proyecto.servicios.exception.GestoPagoCuentaNoEncontradaException;
import com.proyecto.servicios.exception.GestoPagoPagoInvalidoException;
import com.proyecto.servicios.exception.GestoPagoSaldoInsuficienteException;
import com.proyecto.servicios.exception.GestoPagoUsuarioInactivoException;
import com.proyecto.servicios.exception.GestoPagoUsuarioNoEncontradoException;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoPagoRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoReservaPagoService;
import com.proyecto.servicios.service.GestoPagoUpcGenerator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestoPagoReservaPagoServiceImpl implements GestoPagoReservaPagoService {
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter FORMATO_HORA_LOCAL = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final GestoPagoCuentaRepository cuentaRepository;
    private final GestoPagoProductoRepository productoRepository;
    private final GestoPagoUsuarioRepository usuarioRepository;
    private final GestoPagoPagoRepository pagoRepository;
    private final GestoPagoUpcGenerator upcGenerator;
    private final GestoPagoPagoProperties pagoProperties;

    @Override
    @Transactional(transactionManager = "sfTransactionManager")
    public GestoPagoPago reservar(GestoPagoReservaPagoCommand command, Integer usuarioId) {
        BigDecimal monto = validarCommand(command);
        GestoPagoUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(GestoPagoUsuarioNoEncontradoException::new);
        if (!usuario.isActivo() || !usuario.getCliente().isActiva()) {
            throw new GestoPagoUsuarioInactivoException();
        }

        GestoPagoProducto producto = productoRepository.findByIdProducto(command.idProducto())
                .filter(disponible -> Boolean.TRUE.equals(disponible.getActivo())
                        && disponible.getIdServicio() != null)
                .orElseThrow(() -> new GestoPagoPagoInvalidoException("El producto no está disponible para pagos"));
        if (!Objects.equals(producto.getIdServicio(), command.idServicio())
                || !Objects.equals(producto.getTipoFront(), command.tipoFront())) {
            throw new GestoPagoPagoInvalidoException(
                    "El servicio o tipoFront no coincide con el producto del catálogo");
        }
        DatosProveedor datosProveedor = prepararDatosProveedor(command, producto, monto);

        GestoPagoCuenta cuenta = cuentaRepository.findByNumeroCuentaParaRetener(command.numeroCuenta())
                .orElseThrow(GestoPagoCuentaNoEncontradaException::new);
        if (!cuenta.getCliente().getId().equals(usuario.getCliente().getId())) {
            throw new GestoPagoAccesoDenegadoException("No puedes pagar con una cuenta de otro cliente");
        }
        if (!"ACTIVA".equals(cuenta.getEstatus())) {
            throw new GestoPagoCuentaInactivaException();
        }
        if (cuenta.getSaldoDisponible().compareTo(monto) < 0) {
            throw new GestoPagoSaldoInsuficienteException();
        }

        cuenta.setSaldoRetenido(cuenta.getSaldoRetenido().add(monto));

        GestoPagoPago pago = new GestoPagoPago();
        pago.setCuenta(cuenta);
        pago.setProducto(producto);
        pago.setIdServicioProveedor(producto.getIdServicio());
        pago.setIdProductoProveedor(producto.getIdProducto());
        pago.setMontoPago(monto);
        pago.setTelefono(datosProveedor.telefono());
        pago.setReferencia(datosProveedor.referencia());
        pago.setUpc(upcGenerator.generar());
        pago.setHoraLocal(LocalDateTime.now(ZONA).format(FORMATO_HORA_LOCAL));
        pago.setUnidad(pagoProperties.unidad());

        return pagoRepository.save(pago);
    }

    private BigDecimal validarCommand(GestoPagoReservaPagoCommand command) {
        if (command == null || command.numeroCuenta() == null || command.numeroCuenta().isBlank()
                || command.idServicio() == null || command.idServicio() <= 0
                || command.idProducto() == null || command.idProducto() <= 0
                || command.tipoFront() == null || command.tipoFront() <= 0) {
            throw new GestoPagoPagoInvalidoException(
                    "La cuenta, el servicio, el producto y tipoFront son obligatorios");
        }
        if (command.montoPago() == null || command.montoPago().signum() <= 0) {
            throw new GestoPagoPagoInvalidoException("El monto del pago debe ser mayor que cero");
        }
        BigDecimal monto;
        try {
            monto = command.montoPago().setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new GestoPagoPagoInvalidoException("El monto del pago admite como máximo dos decimales");
        }
        if (monto.precision() > 15) {
            throw new GestoPagoPagoInvalidoException("El monto del pago excede el límite permitido");
        }
        return monto;
    }

    private DatosProveedor prepararDatosProveedor(
            GestoPagoReservaPagoCommand command, GestoPagoProducto producto, BigDecimal monto) {
        if (Long.valueOf(10).equals(producto.getIdCatTipoServicio())
                || Long.valueOf(11).equals(producto.getIdCatTipoServicio())) {
            throw new GestoPagoPagoInvalidoException("Los productos con PIN aún no están disponibles");
        }
        if (Integer.valueOf(2).equals(producto.getTipoFront())) {
            if (command.referencia() == null || command.referencia().isBlank()
                    || command.referencia().length() > 100) {
                throw new GestoPagoPagoInvalidoException("La referencia debe tener entre 1 y 100 caracteres");
            }
            return new DatosProveedor("1111111111", command.referencia());
        }
        if (Integer.valueOf(1).equals(producto.getTipoFront())) {
            if (command.telefono() == null || command.telefono().isBlank()
                    || command.telefono().length() < 10 || command.telefono().length() > 20
                    || command.referencia() != null) {
                throw new GestoPagoPagoInvalidoException(
                        "Este producto requiere un teléfono o identificador de 10 a 20 caracteres, sin referencia adicional");
            }
            if (producto.getPrecio() == null || producto.getPrecio().compareTo(monto) != 0) {
                throw new GestoPagoPagoInvalidoException("El monto debe coincidir con el precio del producto");
            }
            return new DatosProveedor(command.telefono(), null);
        }
        throw new GestoPagoPagoInvalidoException("El tipo de producto no está disponible para pagos");
    }

    private record DatosProveedor(String telefono, String referencia) {
    }
}
