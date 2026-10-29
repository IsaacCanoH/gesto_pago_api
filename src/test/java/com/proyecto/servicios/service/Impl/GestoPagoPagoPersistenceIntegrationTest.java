package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;
import com.proyecto.servicios.exception.GestoPagoSaldoInsuficienteException;
import com.proyecto.servicios.exception.GestoPagoPinInvalidoException;
import com.proyecto.servicios.exception.GestoPagoPinNoConfiguradoException;
import com.proyecto.servicios.model.gestopago.GestoPagoFormularioTx;
import com.proyecto.servicios.model.gestopago.GestoPagoReservaPagoCommand;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoClienteRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoCuentaRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoUsuarioRepository;
import com.proyecto.servicios.service.GestoPagoEstadoPagoService;
import com.proyecto.servicios.service.GestoPagoReservaPagoService;
import com.proyecto.servicios.service.GestoPagoPinService;

@EnabledIfEnvironmentVariable(named = "GESTOPAGO_TEST_DB_URL", matches = ".+")
@SpringBootTest(properties = {
        "gestopago.auth.id-distribuidor=1",
        "gestopago.auth.codigo-dispositivo=prueba",
        "gestopago.auth.password=prueba",
        "gestopago.jwt.secret=clave-de-pruebas-con-mas-de-32-caracteres",
        "gestopago.product.sync-cron=0 0 0 1 1 *",
        "gestopago.auth.refresh-cron=0 0 0 1 1 *",
        "gestopago.pago.confirm-initial-delay-ms=3600000",
        "gestopago.pago.manual-review-initial-delay-ms=3600000",
        "spring.cache.type=none"
})
class GestoPagoPagoPersistenceIntegrationTest {
    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("GESTOPAGO_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> System.getenv("GESTOPAGO_TEST_DB_USER"));
        registry.add("spring.datasource.password", () -> System.getenv("GESTOPAGO_TEST_DB_PASSWORD"));
    }

    @Autowired private GestoPagoClienteRepository clientes;
    @Autowired private GestoPagoUsuarioRepository usuarios;
    @Autowired private GestoPagoCuentaRepository cuentas;
    @Autowired private GestoPagoProductoRepository productos;
    @Autowired private GestoPagoReservaPagoService reservas;
    @Autowired private GestoPagoEstadoPagoService estados;
    @Autowired private GestoPagoPinService pines;
    @Autowired private PasswordEncoder encoder;

    @Test
    void pagosConcurrentesNoGastanElMismoSaldoYLiquidacionNoDuplicaElDescuento() throws Exception {
        GestoPagoCliente cliente = new GestoPagoCliente();
        cliente.setNombre("Prueba");
        cliente.setApellidoPaterno("Pagos");
        cliente.setApellidoMaterno("Integracion");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setCurp("PAGI900101HDFABC01");
        cliente.setRfc("PAGI900101ABC");
        cliente.setSexo("HOMBRE");
        cliente.setNacionalidad("MEXICANA");
        cliente.setEstadoCivil("SOLTERO");
        cliente.setTelefonoMovil("5512345678");
        cliente.setOcupacion("Pruebas");
        cliente.setEmpresa("Escuela");
        cliente.setIngresoMensual(new BigDecimal("1000.00"));
        cliente = clientes.save(cliente);

        GestoPagoUsuario usuario = new GestoPagoUsuario();
        usuario.setCliente(cliente);
        usuario.setCorreo("pagos-integracion@example.com");
        usuario.setPasswordHash("hash-de-prueba");
        usuario = usuarios.save(usuario);

        GestoPagoProducto producto = new GestoPagoProducto();
        producto.setIdProducto(14302L);
        producto.setIdServicio(2284L);
        producto.setServicio("SERVICIO");
        producto.setProducto("PRODUCTO 100");
        producto.setTipoFront(1);
        producto.setPrecio(new BigDecimal("100.00"));
        productos.save(producto);

        GestoPagoCuenta cuenta = new GestoPagoCuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta("00000000000000000001");
        cuenta.setSaldo(new BigDecimal("150.00"));
        cuenta = cuentas.save(cuenta);

        int usuarioId = usuario.getId();
        int cuentaId = cuenta.getId();
        assertThrows(GestoPagoPinNoConfiguradoException.class,
                () -> pines.validar(usuarioId, "123456"));
        usuario.setPinHash(encoder.encode("123456"));
        usuarios.saveAndFlush(usuario);
        assertThrows(GestoPagoPinInvalidoException.class,
                () -> pines.validar(usuarioId, "000000"));
        assertEquals(1, usuarios.findById(usuarioId).orElseThrow().getPinIntentosFallidos());
        pines.validar(usuarioId, "123456");
        assertEquals(0, usuarios.findById(usuarioId).orElseThrow().getPinIntentosFallidos());

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);
        try {
            Future<GestoPagoPago> primero = ejecutor.submit(() -> {
                salida.await();
                return reservas.reservar(comando(), usuarioId);
            });
            Future<GestoPagoPago> segundo = ejecutor.submit(() -> {
                salida.await();
                return reservas.reservar(comando(), usuarioId);
            });
            salida.countDown();

            GestoPagoPago aceptado;
            try {
                aceptado = primero.get(40, TimeUnit.SECONDS);
                ExecutionException error = assertThrows(ExecutionException.class,
                        () -> segundo.get(40, TimeUnit.SECONDS));
                assertEquals(GestoPagoSaldoInsuficienteException.class, error.getCause().getClass());
            } catch (ExecutionException e) {
                assertEquals(GestoPagoSaldoInsuficienteException.class, e.getCause().getClass());
                aceptado = segundo.get(40, TimeUnit.SECONDS);
            }

            GestoPagoCuenta despuesDeReserva = cuentas.findById(cuentaId).orElseThrow();
            assertEquals(0, despuesDeReserva.getSaldo().compareTo(new BigDecimal("150.00")));
            assertEquals(0, despuesDeReserva.getSaldoRetenido().compareTo(new BigDecimal("100.00")));

            estados.cancelarSinEnvio(aceptado.getId());
            GestoPagoCuenta liberada = cuentas.findById(cuentaId).orElseThrow();
            assertEquals(0, liberada.getSaldo().compareTo(new BigDecimal("150.00")));
            assertEquals(0, liberada.getSaldoRetenido().compareTo(BigDecimal.ZERO));

            GestoPagoPago nuevoPago = reservas.reservar(comando(), usuarioId);
            estados.iniciarEnvio(nuevoPago.getId(), new GestoPagoFormularioTx("random", "signed"));
            GestoPagoRespuestaEnvio confirmada = new GestoPagoRespuestaEnvio(
                    "01", "Aplicada", "tx-123", "auth-123");
            estados.registrarEnvio(nuevoPago.getId(), confirmada);
            estados.registrarEnvio(nuevoPago.getId(), confirmada);

            GestoPagoCuenta liquidada = cuentas.findById(cuentaId).orElseThrow();
            assertEquals(0, liquidada.getSaldo().compareTo(new BigDecimal("50.00")));
            assertEquals(0, liquidada.getSaldoRetenido().compareTo(BigDecimal.ZERO));
            assertEquals(GestoPagoEstadoPago.EXITOSO,
                    estados.consultar(nuevoPago.getUpc(), usuarioId).estatus());
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private GestoPagoReservaPagoCommand comando() {
        return new GestoPagoReservaPagoCommand("00000000000000000001", 2284L, 14302L, 1,
                new BigDecimal("100.00"), "5512345678", null, UUID.randomUUID());
    }
}
