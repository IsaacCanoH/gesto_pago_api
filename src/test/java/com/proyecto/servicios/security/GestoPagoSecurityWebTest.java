package com.proyecto.servicios.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyecto.servicios.config.GestoPagoSecurityConfig;
import com.proyecto.servicios.controller.GestoPagoAuthController;
import com.proyecto.servicios.controller.GestoPagoClienteController;
import com.proyecto.servicios.controller.GestoPagoCuentaController;
import com.proyecto.servicios.controller.GestoPagoUsuarioController;
import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;
import com.proyecto.servicios.exception.GestoPagoUsuarioInactivoException;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoUsuarioConsultaResponse;
import com.proyecto.servicios.service.GestoPagoAuthService;
import com.proyecto.servicios.service.GestoPagoClienteConsultaService;
import com.proyecto.servicios.service.GestoPagoClienteService;
import com.proyecto.servicios.service.GestoPagoCuentaService;
import com.proyecto.servicios.service.GestoPagoUsuarioService;

@WebMvcTest(controllers = {GestoPagoAuthController.class, GestoPagoUsuarioController.class,
        GestoPagoClienteController.class, GestoPagoCuentaController.class})
@Import({GestoPagoSecurityConfig.class, GestoPagoSecurityErrorWriter.class})
class GestoPagoSecurityWebTest {
    @Autowired private MockMvc mvc;
    @MockBean private GestoPagoAuthService authService;
    @MockBean private GestoPagoUsuarioService usuarioService;
    @MockBean private GestoPagoClienteService clienteService;
    @MockBean private GestoPagoClienteConsultaService consultaService;
    @MockBean private GestoPagoCuentaService cuentaService;
    @MockBean private GestoPagoJwtService jwtService;

    @Test
    void loginPublico() throws Exception {
        when(authService.iniciarSesion(any(GestoPagoLoginRequest.class)))
                .thenReturn(new GestoPagoLoginResponse("token", Instant.now()));

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"cliente@example.com\",\"contrasena\":\"ClaveSegura1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.expiraEn").exists())
                .andExpect(jsonPath("$.tipo").doesNotExist());
    }

    @Test
    void usuarioSinToken() throws Exception {
        mvc.perform(get("/usuarios/7"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(1));
    }

    @Test
    void tokenValido() throws Exception {
        when(jwtService.verificar("token-valido")).thenReturn(7);
        when(authService.obtenerPrincipalActivo(7))
                .thenReturn(new GestoPagoPrincipal(7, "cliente@example.com"));
        when(usuarioService.consultarUsuario(7, 7))
                .thenReturn(new GestoPagoUsuarioConsultaResponse(
                        7, 3, "cliente@example.com", true, null, null));

        mvc.perform(get("/usuarios/7").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void tokenInvalido() throws Exception {
        when(jwtService.verificar("token-malo"))
                .thenThrow(new GestoPagoCredencialesInvalidasException());

        mvc.perform(get("/usuarios/7").header("Authorization", "Bearer token-malo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token inválido o expirado"));
    }

    @Test
    void tokenDeUsuarioInactivo() throws Exception {
        when(jwtService.verificar("token-anterior")).thenReturn(7);
        when(authService.obtenerPrincipalActivo(7))
                .thenThrow(new GestoPagoUsuarioInactivoException());

        mvc.perform(get("/usuarios/7").header("Authorization", "Bearer token-anterior"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("El usuario está inactivo"));
    }

    @Test
    void registroSinToken() throws Exception {
        mvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clienteSinToken() throws Exception {
        mvc.perform(get("/clientes/7"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cambiaSuContrasena() throws Exception {
        when(jwtService.verificar("token-valido")).thenReturn(7);
        when(authService.obtenerPrincipalActivo(7))
                .thenReturn(new GestoPagoPrincipal(7, "cliente@example.com"));

        mvc.perform(put("/usuarios/7/password")
                        .header("Authorization", "Bearer token-valido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"ClaveVieja1!\","
                                + "\"contrasenaNueva\":\"ClaveNueva2!\"}"))
                .andExpect(status().isNoContent());

        verify(usuarioService).cambiarContrasena(eq(7), eq(7), any());
    }

    @Test
    void cancelaCuentaConToken() throws Exception {
        when(jwtService.verificar("token-valido")).thenReturn(7);
        when(authService.obtenerPrincipalActivo(7))
                .thenReturn(new GestoPagoPrincipal(7, "cliente@example.com"));

        mvc.perform(delete("/cuentas/00000000000000000007")
                        .header("Authorization", "Bearer token-valido"))
                .andExpect(status().isNoContent());

        verify(cuentaService).cancelarCuenta("00000000000000000007", 7);
    }

    @Test
    void noCancelaCuentaSinToken() throws Exception {
        mvc.perform(delete("/cuentas/00000000000000000007"))
                .andExpect(status().isUnauthorized());
    }
}
