package com.proyecto.servicios.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyecto.servicios.config.GestoPagoSecurityConfig;
import com.proyecto.servicios.controller.GestoPagoAuthController;
import com.proyecto.servicios.controller.GestoPagoClienteController;
import com.proyecto.servicios.controller.GestoPagoCuentaController;
import com.proyecto.servicios.controller.GestoPagoUsuarioController;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoUsuarioConsultaResponse;
import com.proyecto.servicios.service.GestoPagoAuthService;
import com.proyecto.servicios.service.GestoPagoClienteBusquedaResultado;
import com.proyecto.servicios.service.GestoPagoClienteConsultaService;
import com.proyecto.servicios.service.GestoPagoClienteService;
import com.proyecto.servicios.service.GestoPagoCuentaService;
import com.proyecto.servicios.service.GestoPagoUsuarioService;

@WebMvcTest(controllers = {GestoPagoAuthController.class, GestoPagoUsuarioController.class,
        GestoPagoClienteController.class, GestoPagoCuentaController.class})
@Import(GestoPagoSecurityConfig.class)
class GestoPagoSecurityWebTest {
    @Autowired private MockMvc mvc;
    @MockBean private GestoPagoAuthService authService;
    @MockBean private GestoPagoUsuarioService usuarioService;
    @MockBean private GestoPagoClienteService clienteService;
    @MockBean private GestoPagoClienteConsultaService consultaService;
    @MockBean private GestoPagoCuentaService cuentaService;

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
    void usuarioSinToken() throws Exception {
        when(usuarioService.consultarUsuario(7, null))
                .thenReturn(new GestoPagoUsuarioConsultaResponse(
                        7, 3, "cliente@example.com", true, null, null));

        mvc.perform(post("/usuarios/consultar")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"id\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.id").value(7));
    }

    @Test
    void tokenInvalidoNoBloqueaConsulta() throws Exception {
        when(usuarioService.consultarUsuario(7, null))
                .thenReturn(new GestoPagoUsuarioConsultaResponse(
                        7, 3, "cliente@example.com", true, null, null));

        mvc.perform(post("/usuarios/consultar")
                        .header("Authorization", "Bearer token-vencido")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"id\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.id").value(7));
    }

    @Test
    void registroSinTokenConservaValidaciones() throws Exception {
        mvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clientesSinToken() throws Exception {
        when(consultaService.buscarClientes(any()))
                .thenReturn(new GestoPagoClienteBusquedaResultado(Page.empty(), null));

        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.resultados.content").isArray());
    }

    @Test
    void rutaInexistenteSinTokenDevuelve404() throws Exception {
        mvc.perform(get("/ruta-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La ruta solicitada no existe"));
    }

    @Test
    void cambioContrasenaSinTokenUsaIdDelBody() throws Exception {
        mvc.perform(put("/usuarios/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":7,\"contrasenaActual\":\"ClaveVieja1!\","
                                + "\"contrasenaNueva\":\"ClaveNueva2!\"}"))
                .andExpect(status().isOk());

        verify(usuarioService).cambiarContrasena(org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.isNull(), any());
    }

    @Test
    void cambioContrasenaSinIdSeRechaza() throws Exception {
        mvc.perform(put("/usuarios/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"ClaveVieja1!\","
                                + "\"contrasenaNueva\":\"ClaveNueva2!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.id").exists());
    }

    @Test
    void cancelaCuentaSinToken() throws Exception {
        mvc.perform(post("/cuentas/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"00000000000000000007\"}"))
                .andExpect(status().isOk());

        verify(cuentaService).cancelarCuenta("00000000000000000007", null);
    }
}
