package com.proyecto.servicios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.proyecto.servicios.service.GestoPagoClienteBusquedaResultado;
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
        when(consultas.buscarClientes(any())).thenReturn(new GestoPagoClienteBusquedaResultado(
                new PageImpl<>(List.of(resumen())), respuesta()));

        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":7,\"detalle\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.detalle.id").value(7));
    }

    @Test
    void otrasBusquedas() throws Exception {
        when(consultas.buscarClientes(any())).thenReturn(new GestoPagoClienteBusquedaResultado(
                new PageImpl<>(List.of(resumen())), respuesta()));

        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"curp\":\"CANO050429HKNTEW00\",\"detalle\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.datos.detalle.id").value(7));
        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rfc\":\"CANO0504295G3\",\"detalle\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.datos.detalle.id").value(7));
        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"00000000000000000007\",\"detalle\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.datos.detalle.id").value(7));
        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"cliente@example.com\",\"detalle\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.datos.detalle.id").value(7));
    }

    @Test
    void listaClientes() throws Exception {
        when(consultas.buscarClientes(any())).thenReturn(new GestoPagoClienteBusquedaResultado(
                new PageImpl<>(List.of(resumen())), null));

        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.resultados.content[0].id").value(7))
                .andExpect(jsonPath("$.datos.resultados.content[0].correo").value("cliente@example.com"))
                .andExpect(jsonPath("$.datos.resultados.content[0].domicilio").doesNotExist())
                .andExpect(jsonPath("$.datos.resultados.content[0].cuentas").doesNotExist());
    }

    @Test
    void filtros() throws Exception {
        when(consultas.buscarClientes(any())).thenReturn(new GestoPagoClienteBusquedaResultado(
                new PageImpl<>(List.of(resumen())), null));

        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activa\":true,\"desde\":\"2026-10-05\",\"hasta\":\"2026-10-05\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.resultados.content[0].id").value(7));
    }

    @Test
    void buscaPorCurpYRfc() throws Exception {
        when(consultas.buscarClientes(any())).thenReturn(new GestoPagoClienteBusquedaResultado(
                new PageImpl<>(List.of(resumen())), null));

        mvc.perform(post("/clientes/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"curp\":\"can\",\"rfc\":\"5g3\",\"parcial\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.resultados.content[0].id").value(7))
                .andExpect(jsonPath("$.datos.resultados.content[0].correo").value("cliente@example.com"));
    }

    @Test
    void actualizaCliente() throws Exception {
        mvc.perform(put("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cambioCompleto()))
                .andExpect(status().isOk());

        verify(clientes).actualizarCliente(eq(7), any());
    }

    @Test
    void actualizaUnCampo() throws Exception {
        mvc.perform(patch("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":7,\"ocupacion\":\"Analista\"}"))
                .andExpect(status().isOk());

        verify(clientes).actualizarClienteParcial(eq(7), any());
    }

    @Test
    void daDeBaja() throws Exception {
        mvc.perform(post("/clientes/desactivar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":7}"))
                .andExpect(status().isOk());

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
                  "id": 7,
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
