package com.proyecto.servicios.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyecto.servicios.model.gestopago.GestoPagoCuentaBusquedaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;
import com.proyecto.servicios.service.GestoPagoCuentaService;

@WebMvcTest(GestoPagoCuentaController.class)
@AutoConfigureMockMvc(addFilters = false)
class GestoPagoCuentaControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private GestoPagoCuentaService cuentas;

    @Test
    void consultaSaldo() throws Exception {
        when(cuentas.buscarCuentas(org.mockito.ArgumentMatchers.any(GestoPagoCuentaBusquedaRequest.class)))
                .thenReturn(new PageImpl<>(List.of(respuesta())));

        mvc.perform(post("/cuentas/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"00000000000000000007\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.content[0].saldo").value(150.00))
                .andExpect(jsonPath("$.datos.content[0].estatus").value("ACTIVA"));
    }

    @Test
    void listaActivas() throws Exception {
        when(cuentas.buscarCuentas(org.mockito.ArgumentMatchers.any(GestoPagoCuentaBusquedaRequest.class)))
                .thenReturn(new PageImpl<>(List.of(respuesta())));

        mvc.perform(post("/cuentas/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"soloActivas\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.content[0].numeroCuenta")
                        .value("00000000000000000007"));
    }

    @Test
    void numeroCuentaInvalido() throws Exception {
        mvc.perform(post("/cuentas/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"desconocida\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numeroCuenta").exists());
    }

    private GestoPagoCuentaConsultaResponse respuesta() {
        return new GestoPagoCuentaConsultaResponse(
                "00000000000000000007", new BigDecimal("150.00"), "ACTIVA");
    }
}
