package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.config.GestoPagoPagoProperties;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoAccesoDenegadoException;
import com.proyecto.servicios.exception.GestoPagoCuentaInactivaException;
import com.proyecto.servicios.exception.GestoPagoPagoInvalidoException;
import com.proyecto.servicios.exception.GestoPagoSaldoInsuficienteException;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoPagoRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoUpcGenerator;

@ExtendWith(MockitoExtension.class)
class GestoPagoReservaPagoServiceImplTest {
    private static final String NUMERO_CUENTA = "00000000000000000007";

    @Mock private GestoPagoCuentaRepository cuentas;
    @Mock private GestoPagoProductoRepository productos;
    @Mock private GestoPagoUsuarioRepository usuarios;
    @Mock private GestoPagoPagoRepository pagos;
    @Mock private GestoPagoUpcGenerator upcGenerator;

    private GestoPagoReservaPagoServiceImpl servicio;
    private GestoPagoCuenta cuenta;

    @BeforeEach
    void preparar() {
        servicio = new GestoPagoReservaPagoServiceImpl(
                cuentas, productos, usuarios, pagos, upcGenerator,
                new GestoPagoPagoProperties("083"));
    }

    private GestoPagoProducto prepararRepositorios() {
        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setId(7);
        GestoPagoUsuario usuario = new GestoPagoUsuario();
        usuario.setCliente(cliente);
        when(usuarios.findById(5)).thenReturn(Optional.of(usuario));

        GestoPagoProducto producto = new GestoPagoProducto();
        producto.setIdProducto(123L);
        producto.setIdServicio(456L);
        producto.setTipoFront(2);
        when(productos.findByIdProducto(123L)).thenReturn(Optional.of(producto));

        cuenta = new GestoPagoCuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta(NUMERO_CUENTA);
        cuenta.setSaldo(new BigDecimal("100.00"));
        return producto;
    }

    private void prepararCuenta() {
        when(cuentas.findByNumeroCuentaParaRetener(NUMERO_CUENTA))
                .thenReturn(Optional.of(cuenta));
    }

    @Test
    void retieneSinDescontarSaldoYGuardaPagoPendiente() {
        prepararRepositorios();
        prepararCuenta();
        when(upcGenerator.generar()).thenReturn("P12345678901234");
        when(pagos.save(any(GestoPagoPago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        GestoPagoPago pago = servicio.reservar(command("40.00"), 5);

        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("40.00"), cuenta.getSaldoRetenido());
        assertEquals(new BigDecimal("60.00"), cuenta.getSaldoDisponible());
        assertEquals(GestoPagoEstadoPago.PENDIENTE, pago.getEstatus());
        assertEquals(new BigDecimal("40.00"), pago.getMontoPago());
        assertEquals(123L, pago.getIdProductoProveedor());
        assertEquals(456L, pago.getIdServicioProveedor());
        assertEquals("P12345678901234", pago.getUpc());
        assertEquals("083", pago.getUnidad());
        assertEquals("1111111111", pago.getTelefono());
        assertEquals("REFERENCIA123", pago.getReferencia());
        assertTrue(pago.getHoraLocal().matches("[0-9]{14}"));
        verify(cuentas).findByNumeroCuentaParaRetener(NUMERO_CUENTA);
    }

    @Test
    void tomaEnCuentaRetencionesExistentes() {
        prepararRepositorios();
        prepararCuenta();
        cuenta.setSaldoRetenido(new BigDecimal("70.00"));

        assertThrows(GestoPagoSaldoInsuficienteException.class,
                () -> servicio.reservar(command("40.00"), 5));

        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("70.00"), cuenta.getSaldoRetenido());
        verifyNoInteractions(pagos);
    }

    @Test
    void rechazaCuentaInactiva() {
        prepararRepositorios();
        prepararCuenta();
        cuenta.setEstatus("INACTIVA");

        assertThrows(GestoPagoCuentaInactivaException.class,
                () -> servicio.reservar(command("40.00"), 5));

        assertEquals(BigDecimal.ZERO, cuenta.getSaldoRetenido());
        verifyNoInteractions(pagos);
    }

    @Test
    void rechazaCuentaDeOtroCliente() {
        prepararRepositorios();
        prepararCuenta();
        GestoPagoCliente otroCliente = new GestoPagoCliente();
        otroCliente.setId(8);
        cuenta.setCliente(otroCliente);

        assertThrows(GestoPagoAccesoDenegadoException.class,
                () -> servicio.reservar(command("40.00"), 5));

        verifyNoInteractions(pagos);
    }

    @Test
    void rechazaMontoConMasDeDosDecimales() {
        assertThrows(GestoPagoPagoInvalidoException.class,
                () -> servicio.reservar(command("40.001"), 5));

        verifyNoInteractions(pagos);
    }

    @Test
    void respetaPrecioDeProductoTelefonico() {
        GestoPagoProducto producto = prepararRepositorios();
        producto.setTipoFront(1);
        producto.setPrecio(new BigDecimal("50.00"));

        assertThrows(GestoPagoPagoInvalidoException.class,
                () -> servicio.reservar(new GestoPagoReservaPagoCommand(
                        NUMERO_CUENTA, 456L, 123L, 1,
                        new BigDecimal("40.00"), "5512345678", null), 5));
        verifyNoInteractions(pagos);
    }

    @Test
    void productoTelefonicoSinPrecioNoAceptaUnMontoElegidoPorElCliente() {
        GestoPagoProducto producto = prepararRepositorios();
        producto.setTipoFront(1);
        producto.setPrecio(null);

        assertThrows(GestoPagoPagoInvalidoException.class,
                () -> servicio.reservar(new GestoPagoReservaPagoCommand(
                        NUMERO_CUENTA, 456L, 123L, 1,
                        new BigDecimal("40.00"), "5512345678", null), 5));
        verifyNoInteractions(pagos);
    }

    @Test
    void reservaProductoTelefonicoSinReferenciaAdicional() {
        GestoPagoProducto producto = prepararRepositorios();
        producto.setTipoFront(1);
        producto.setPrecio(new BigDecimal("40.00"));
        prepararCuenta();
        when(upcGenerator.generar()).thenReturn("P12345678901234");
        when(pagos.save(any(GestoPagoPago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        GestoPagoPago pago = servicio.reservar(new GestoPagoReservaPagoCommand(
                NUMERO_CUENTA, 456L, 123L, 1,
                new BigDecimal("40.00"), "5512345678", null), 5);

        assertEquals("5512345678", pago.getTelefono());
        assertEquals(null, pago.getReferencia());
        assertEquals(new BigDecimal("40.00"), cuenta.getSaldoRetenido());
    }

    @Test
    void pagoDeServicioRequiereReferencia() {
        prepararRepositorios();

        assertThrows(GestoPagoPagoInvalidoException.class,
                () -> servicio.reservar(new GestoPagoReservaPagoCommand(
                        NUMERO_CUENTA, 456L, 123L, 2,
                        new BigDecimal("40.00"), null, null), 5));
        verifyNoInteractions(pagos);
    }

    @Test
    void rechazaServicioOTipoFrontQueNoCorrespondenAlProducto() {
        prepararRepositorios();

        assertThrows(GestoPagoPagoInvalidoException.class,
                () -> servicio.reservar(new GestoPagoReservaPagoCommand(
                        NUMERO_CUENTA, 999L, 123L, 2,
                        new BigDecimal("40.00"), null, "REFERENCIA123"), 5));
        assertThrows(GestoPagoPagoInvalidoException.class,
                () -> servicio.reservar(new GestoPagoReservaPagoCommand(
                        NUMERO_CUENTA, 456L, 123L, 1,
                        new BigDecimal("40.00"), null, "REFERENCIA123"), 5));
        verifyNoInteractions(cuentas, pagos);
    }

    private GestoPagoReservaPagoCommand command(String monto) {
        return new GestoPagoReservaPagoCommand(NUMERO_CUENTA, 456L, 123L, 2,
                new BigDecimal(monto), null, "REFERENCIA123");
    }
}
