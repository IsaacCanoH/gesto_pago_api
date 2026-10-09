package com.proyecto.servicios.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;

import com.proyecto.servicios.exception.GestoPagoCuentaNoEncontradaException;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;
import com.proyecto.servicios.service.GestoPagoCuentaService;

@WebMvcTest(GestoPagoCuentaController.class)
@AutoConfigureMockMvc(addFilters = false)
class GestoPagoCuentaControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private GestoPagoCuentaService cuentas;

    @Test
    void consultaSaldo() throws Exception {
        when(cuentas.consultarCuenta("00000000000000000007")).thenReturn(respuesta());

        mvc.perform(get("/cuentas/00000000000000000007"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(150.00))
                .andExpect(jsonPath("$.estatus").value("ACTIVA"));
    }

    @Test
    void listaActivas() throws Exception {
        when(cuentas.consultarCuentasActivas(0, 20))
                .thenReturn(new PageImpl<>(List.of(respuesta())));

        mvc.perform(get("/cuentas/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numeroCuenta")
                        .value("00000000000000000007"));
    }

    @Test
    void cuentaNoEncontrada() throws Exception {
        when(cuentas.consultarCuenta("desconocida"))
                .thenThrow(new GestoPagoCuentaNoEncontradaException());

        mvc.perform(get("/cuentas/desconocida"))
                .andExpect(status().isNotFound());
    }

    private GestoPagoCuentaConsultaResponse respuesta() {
        return new GestoPagoCuentaConsultaResponse(
                "00000000000000000007", new BigDecimal("150.00"), "ACTIVA");
    }
}
