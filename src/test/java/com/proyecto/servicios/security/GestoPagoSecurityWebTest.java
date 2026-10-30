package com.proyecto.servicios.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.proyecto.servicios.config.GestoPagoSecurityConfig;
import com.proyecto.servicios.controller.GestoPagoAuthController;
import com.proyecto.servicios.controller.GestoPagoClienteController;
import com.proyecto.servicios.controller.GestoPagoCuentaController;
import com.proyecto.servicios.controller.GestoPagoPagoController;
import com.proyecto.servicios.controller.GestoPagoUsuarioController;
import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaResponse;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;
import com.proyecto.servicios.service.GestoPagoAuthService;
import com.proyecto.servicios.service.GestoPagoClienteBusquedaResultado;
import com.proyecto.servicios.service.GestoPagoClienteConsultaService;
import com.proyecto.servicios.service.GestoPagoClienteService;
import com.proyecto.servicios.service.GestoPagoCuentaService;
import com.proyecto.servicios.service.GestoPagoEstadoPagoService;
import com.proyecto.servicios.service.GestoPagoPagoWorkflowService;
import com.proyecto.servicios.service.GestoPagoUsuarioService;

@WebMvcTest(controllers = {GestoPagoAuthController.class, GestoPagoUsuarioController.class,
        GestoPagoClienteController.class, GestoPagoCuentaController.class,
        GestoPagoPagoController.class})
@Import({GestoPagoSecurityConfig.class, GestoPagoSecurityErrorWriter.class})
class GestoPagoSecurityWebTest {
    @Autowired private MockMvc mvc;
    @MockBean private GestoPagoAuthService authService;
    @MockBean private GestoPagoUsuarioService usuarioService;
    @MockBean private GestoPagoClienteService clienteService;
    @MockBean private GestoPagoClienteConsultaService consultaService;
    @MockBean private GestoPagoCuentaService cuentaService;
    @MockBean private GestoPagoEstadoPagoService estadoPagoService;
    @MockBean private GestoPagoPagoWorkflowService pagoWorkflowService;
    @MockBean private GestoPagoJwtService jwtService;

    @Test
    void loginSigueDisponible() throws Exception {
        when(authService.iniciarSesion(any(GestoPagoLoginRequest.class)))
                .thenReturn(new GestoPagoLoginResponse("token", Instant.now()));

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"cliente@example.com\",\"contrasena\":\"ClaveSegura1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.token").value("token"));
    }

    @Test
    void usuarioSinTokenSeRechaza() throws Exception {
        mvc.perform(post("/usuarios/consultar")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"id\":7}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoSeRechaza() throws Exception {
        when(jwtService.verificar("token-vencido"))
                .thenThrow(new GestoPagoCredencialesInvalidasException());
        mvc.perform(post("/usuarios/consultar")
                        .header("Authorization", "Bearer token-vencido")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"id\":7}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroSinTokenSeRechazaAntesDeValidar() throws Exception {
        mvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientesConTokenConservaSuRespuesta() throws Exception {
        autenticar();
        when(consultaService.buscarClientes(any()))
                .thenReturn(new GestoPagoClienteBusquedaResultado(Page.empty(), null));

        mvc.perform(post("/clientes/buscar")
                        .header("Authorization", "Bearer token-valido")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.resultados.content").isArray());
    }

    @Test
    void rutaInexistenteConTokenDevuelve404() throws Exception {
        autenticar();
        mvc.perform(get("/ruta-inexistente")
                        .header("Authorization", "Bearer token-valido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La ruta solicitada no existe"));
    }

    @Test
    void consultarPagoExigeTokenYUsaUsuarioAutenticado() throws Exception {
        String cuerpo = "{\"upc\":\"P12345678901234\"}";
        mvc.perform(post("/pagos/consultar")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());

        autenticar();
        when(estadoPagoService.consultar("P12345678901234", 7))
                .thenReturn(new GestoPagoPagoConsultaResponse(
                        "P12345678901234", "00000000000000000007", 205L,
                        java.math.BigDecimal.TEN, GestoPagoEstadoPago.PENDIENTE,
                        false, Instant.now(), null, null, null));

        var resultado = mvc.perform(post("/pagos/consultar")
                        .header("Authorization", "Bearer token-valido")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
        verify(jwtService).verificar("token-valido");
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("PENDIENTE"));
        verify(estadoPagoService).consultar("P12345678901234", 7);
    }

    @Test
    void iniciarPagoExigeToken() throws Exception {
        mvc.perform(post("/pagos")
                        .header("Idempotency-Key", "e5c1b457-3e9e-4b48-9d7c-aaa501c5e271")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"00000000000000000007\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cambioContrasenaConTokenUsaIdDelBody() throws Exception {
        autenticar();
        mvc.perform(put("/usuarios/password")
                        .header("Authorization", "Bearer token-valido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":7,\"contrasenaActual\":\"ClaveVieja1!\","
                                + "\"contrasenaNueva\":\"ClaveNueva2!\"}"))
                .andExpect(status().isOk());

        verify(usuarioService).cambiarContrasena(org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.isNull(), any());
    }

    @Test
    void cambioContrasenaSinIdSeRechazaConToken() throws Exception {
        autenticar();
        mvc.perform(put("/usuarios/password")
                        .header("Authorization", "Bearer token-valido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"ClaveVieja1!\","
                                + "\"contrasenaNueva\":\"ClaveNueva2!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.id").exists());
    }

    @Test
    void cancelaCuentaConToken() throws Exception {
        autenticar();
        mvc.perform(post("/cuentas/cancelar")
                        .header("Authorization", "Bearer token-valido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"00000000000000000007\"}"))
                .andExpect(status().isOk());

        verify(cuentaService).cancelarCuenta("00000000000000000007", null);
    }

    @ParameterizedTest
    @MethodSource("rutasProtegidas")
    void rutasDeNegocioExigenToken(MockHttpServletRequestBuilder request) throws Exception {
        mvc.perform(request).andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> rutasProtegidas() {
        return Stream.of(
                post("/clientes"), put("/clientes"), patch("/clientes"),
                post("/clientes/desactivar"), post("/clientes/buscar"),
                post("/cuentas/buscar"), post("/cuentas/cancelar"),
                post("/pagos"), post("/pagos/consultar"),
                get("/gestopago/productos"), post("/gestopago/productos/paginar"),
                post("/gestopago/productos/sincronizar"),
                post("/usuarios/consultar"), put("/usuarios/password"),
                post("/personas"), put("/personas"), post("/personas/eliminar"))
                .map(Arguments::of);
    }

    private void autenticar() {
        when(jwtService.verificar("token-valido")).thenReturn(7);
        when(authService.obtenerPrincipalActivo(7))
                .thenReturn(new GestoPagoPrincipal(7, "cliente@example.com"));
    }
}
