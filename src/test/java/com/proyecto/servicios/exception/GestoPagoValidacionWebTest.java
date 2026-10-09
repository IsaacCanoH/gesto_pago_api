package com.proyecto.servicios.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyecto.servicios.controller.GestoPagoClienteController;
import com.proyecto.servicios.exception.GestoPagoClienteNoEncontradoException;
import com.proyecto.servicios.exception.GestoPagoCorreoDuplicadoException;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroResponse;
import com.proyecto.servicios.service.GestoPagoClienteConsultaService;
import com.proyecto.servicios.service.GestoPagoClienteService;

@WebMvcTest(GestoPagoClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class GestoPagoValidacionWebTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private GestoPagoClienteService clienteService;

    @MockBean
    private GestoPagoClienteConsultaService consultaService;

    @Test
    void paginaNoNumerica() throws Exception {
        mvc.perform(get("/clientes").param("pagina", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Parámetro pagina: debe ser un número entero"));
    }

    @Test
    void fechaMalEscrita() throws Exception {
        mvc.perform(get("/clientes/fecha-creacion")
                        .param("desde", "2026/10/05")
                        .param("hasta", "2026-10-05"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Parámetro desde: debe tener formato AAAA-MM-DD"));
    }

    @Test
    void faltaFecha() throws Exception {
        mvc.perform(get("/clientes/fecha-creacion")
                        .param("hasta", "2026-10-05"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Parámetro desde: es obligatorio"));
    }

    @Test
    void jsonInvalido() throws Exception {
        mvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "El JSON es inválido o contiene un dato con formato incorrecto"));
    }

    @Test
    void fechaInvalida() throws Exception {
        mvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaNacimiento\":\"2000/01/01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "La fecha debe tener formato AAAA-MM-DD"));
    }

    @Test
    void recortaEspacios() throws Exception {
        when(clienteService.registrarCliente(any(GestoPagoClienteRegistroRequest.class)))
                .thenReturn(new GestoPagoClienteRegistroResponse(
                        1, "00000000000000000001", BigDecimal.ZERO));

        mvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "  Maria Jose  ",
                                  "segundoNombre": "   ",
                                  "apellidoPaterno": "Lopez    ",
                                  "apellidoMaterno": "Perez",
                                  "fechaNacimiento": "2000-01-01",
                                  "curp": "  CANO050429HKNTEW00  ",
                                  "rfc": "  CANO0504295G3  ",
                                  "correo": "  cliente@example.com  ",
                                  "contrasena": " ClaveSegura1! ",
                                  "telefonoMovil": " 5524698569 ",
                                  "sexo": "HOMBRE",
                                  "nacionalidad": "MEXICANA",
                                  "estadoCivil": "SOLTERO",
                                  "ocupacion": "Tester",
                                  "empresa": "Empresa Test",
                                  "ingresoMensual": 100.00,
                                  "domicilio": {
                                    "calle": "  Calle Reforma  ",
                                    "numeroExterior": "3B",
                                    "numeroInterior": "   ",
                                    "colonia": "Centro",
                                    "municipio": "Dolores Hidalgo",
                                    "estado": "Guanajuato",
                                    "codigoPostal": " 37803 ",
                                    "pais": "Mexico"
                                  }
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<GestoPagoClienteRegistroRequest> captor =
                ArgumentCaptor.forClass(GestoPagoClienteRegistroRequest.class);
        verify(clienteService).registrarCliente(captor.capture());
        var request = captor.getValue();
        assertEquals("Maria Jose", request.getNombre());
        assertEquals("Lopez", request.getApellidoPaterno());
        assertEquals("CANO0504295G3", request.getRfc());
        assertEquals("cliente@example.com", request.getCorreo());
        assertEquals("Calle Reforma", request.getDomicilio().getCalle());
        assertEquals("37803", request.getDomicilio().getCodigoPostal());
        assertNull(request.getSegundoNombre());
        assertNull(request.getDomicilio().getNumeroInterior());
        assertEquals(" ClaveSegura1! ", request.getContrasena());
    }

    @Test
    void rechazaApellidoVacio() throws Exception {
        mvc.perform(patch("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apellidoPaterno\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clienteService);
    }

    @Test
    void rechazaCodigoPostal() throws Exception {
        mvc.perform(patch("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"domicilio\":{\"codigoPostal\":\"123\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Campo domicilio.codigoPostal: debe tener exactamente 5 dígitos"));
        verifyNoInteractions(clienteService);
    }

    @Test
    void clienteNoEncontrado() throws Exception {
        when(consultaService.consultarCliente(99))
                .thenThrow(new GestoPagoClienteNoEncontradoException());

        mvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado"));
    }

    @Test
    void correoDuplicado() throws Exception {
        doThrow(new GestoPagoCorreoDuplicadoException())
                .when(clienteService).actualizarClienteParcial(eq(7), any());

        mvc.perform(patch("/clientes/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ocupado@example.com\"}"))
                .andExpect(status().isConflict());
    }
}
