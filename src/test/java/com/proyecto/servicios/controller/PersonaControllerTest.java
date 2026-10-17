package com.proyecto.servicios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyecto.servicios.exception.PersonaNoEncontradaException;
import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.service.PersonaService;

@WebMvcTest(PersonaController.class)
@AutoConfigureMockMvc(addFilters = false)
class PersonaControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private PersonaService personas;

    @Test
    void creaPersonaConRespuestaUniforme() throws Exception {
        when(personas.creaPersona(any(PersonasRequest.class)))
                .thenReturn(new PersonaResponse("Ana", "López", "Pérez"));

        mvc.perform(post("/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"apellidoP\":\"López\","
                                + "\"apellidoMaterno\":\"Pérez\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(0))
                .andExpect(jsonPath("$.datos.nombre").value("Ana"));
    }

    @Test
    void validaCamposDePersona() throws Exception {
        mvc.perform(post("/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.apellidoP").exists());
    }

    @Test
    void personaNoEncontradaNoSeOcultaComoExito() throws Exception {
        org.mockito.Mockito.doThrow(new PersonaNoEncontradaException())
                .when(personas).eliminaPersona(any(EliminaPersonaRequest.class));

        mvc.perform(post("/personas/eliminar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Desconocida\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La persona no existe"));
    }
}
