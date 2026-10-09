package com.proyecto.servicios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyecto.servicios.model.gestopago.GestoPagoClienteConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteResumenResponse;
import com.proyecto.servicios.service.GestoPagoClienteConsultaService;
import com.proyecto.servicios.service.GestoPagoClienteService;

@WebMvcTest(GestoPagoClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class GestoPagoClienteControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private GestoPagoClienteService clientes;
    @MockBean private GestoPagoClienteConsultaService consultas;

    @Test
    void consultaPorId() throws Exception {
        when(consultas.consultarCliente(7)).thenReturn(respuesta());

        mvc.perform(get("/clientes/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void otrasBusquedas() throws Exception {
        when(consultas.consultarClientePorCurp("CANO050429HKNTEW00"))
                .thenReturn(respuesta());
        when(consultas.consultarClientePorRfc("CANO0504295G3"))
                .thenReturn(respuesta());
        when(consultas.consultarClientePorNumeroCuenta("00000000000000000007"))
                .thenReturn(respuesta());
        when(consultas.consultarClientePorCorreo("cliente@example.com"))
                .thenReturn(respuesta());

        mvc.perform(get("/clientes/curp/CANO050429HKNTEW00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
        mvc.perform(get("/clientes/rfc/CANO0504295G3"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
        mvc.perform(get("/clientes/cuenta/00000000000000000007"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
        mvc.perform(get("/clientes/correo/cliente@example.com"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void listaClientes() throws Exception {
        when(consultas.consultarClientes(0, 20))
                .thenReturn(new PageImpl<>(List.of(resumen())));

        mvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].correo").value("cliente@example.com"))
                .andExpect(jsonPath("$.content[0].domicilio").doesNotExist())
                .andExpect(jsonPath("$.content[0].cuentas").doesNotExist());
    }

    @Test
    void filtros() throws Exception {
        LocalDate dia = LocalDate.of(2026, 10, 5);
        when(consultas.consultarClientesActivos(0, 20))
                .thenReturn(new PageImpl<>(List.of(resumen())));
        when(consultas.consultarClientesPorFechaCreacion(dia, dia, 0, 20))
                .thenReturn(new PageImpl<>(List.of(resumen())));

        mvc.perform(get("/clientes/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7));
        mvc.perform(get("/clientes/fecha-creacion")
                        .param("desde", "2026-10-05")
                        .param("hasta", "2026-10-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7));
    }

    @Test
    void actualizaCliente() throws Exception {
        mvc.perform(put("/clientes/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cambioCompleto()))
                .andExpect(status().isNoContent());

        verify(clientes).actualizarCliente(eq(7), any());
    }

    @Test
    void actualizaUnCampo() throws Exception {
        mvc.perform(patch("/clientes/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ocupacion\":\"Analista\"}"))
                .andExpect(status().isNoContent());

        verify(clientes).actualizarClienteParcial(eq(7), any());
    }

    @Test
    void daDeBaja() throws Exception {
        mvc.perform(delete("/clientes/7"))
                .andExpect(status().isNoContent());

        verify(clientes).desactivarCliente(7);
    }

    private GestoPagoClienteConsultaResponse respuesta() {
        var response = new GestoPagoClienteConsultaResponse();
        response.setId(7);
        return response;
    }

    private GestoPagoClienteResumenResponse resumen() {
        return new GestoPagoClienteResumenResponse(
                7, "María", "López", "Pérez", "cliente@example.com", true,
                LocalDate.of(2026, 10, 5));
    }

    private String cambioCompleto() {
        return """
                {
                  "nombre": "Maria",
                  "apellidoPaterno": "Lopez",
                  "apellidoMaterno": "Perez",
                  "fechaNacimiento": "2000-01-01",
                  "correo": "cliente@example.com",
                  "telefonoMovil": "5512345678",
                  "sexo": "MUJER",
                  "nacionalidad": "MEXICANA",
                  "estadoCivil": "SOLTERA",
                  "ocupacion": "Analista",
                  "empresa": "Empresa Test",
                  "ingresoMensual": 9000.00,
                  "domicilio": {
                    "calle": "Calle Reforma",
                    "numeroExterior": "3B",
                    "colonia": "Centro",
                    "municipio": "Dolores Hidalgo",
                    "estado": "Guanajuato",
                    "codigoPostal": "37803",
                    "pais": "Mexico"
                  }
                }
                """;
    }
}
