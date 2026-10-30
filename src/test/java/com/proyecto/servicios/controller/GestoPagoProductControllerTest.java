package com.proyecto.servicios.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.proyecto.servicios.service.GestoPagoProductService;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoPaginaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.http.MediaType;

@WebMvcTest(GestoPagoProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class GestoPagoProductControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private GestoPagoProductService productos;

    @Test
    void sincronizacionRespondeConDto() throws Exception {
        when(productos.sincronizarProductos()).thenReturn(3);

        mvc.perform(post("/gestopago/productos/sincronizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(0))
                .andExpect(jsonPath("$.datos.productosSincronizados").value(3));
    }

    @Test
    void consultaSinFiltrosConservaGet() throws Exception {
        when(productos.consultarProductos()).thenReturn(List.of());

        mvc.perform(get("/gestopago/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").isArray());
    }

    @Test
    void consultaProductosPaginadosConMetadatos() throws Exception {
        GestoPagoProductoResponse producto = new GestoPagoProductoResponse();
        producto.setIdProducto(14302L);
        when(productos.consultarProductosPaginados(any(GestoPagoProductoPaginaRequest.class)))
                .thenReturn(new PageImpl<>(List.of(producto), PageRequest.of(1, 2), 3));

        mvc.perform(post("/gestopago/productos/paginar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pagina\":1,\"tamanio\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.content[0].idProducto").value(14302))
                .andExpect(jsonPath("$.datos.page.number").value(1))
                .andExpect(jsonPath("$.datos.page.totalElements").value(3));
    }

    @Test
    void rechazaTamanoMayorAlLimite() throws Exception {
        mvc.perform(post("/gestopago/productos/paginar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pagina\":0,\"tamanio\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.tamanio").value("no debe superar 100"));
    }

    @Test
    void rutaInexistenteDevuelve404Estructurado() throws Exception {
        mvc.perform(get("/ruta-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(1))
                .andExpect(jsonPath("$.mensaje").value("La ruta solicitada no existe"));

        mvc.perform(post("/ruta-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(1))
                .andExpect(jsonPath("$.mensaje").value("La ruta solicitada no existe"));
    }

    @Test
    void metodoIncorrectoEnRutaExistenteDevuelve405() throws Exception {
        mvc.perform(get("/gestopago/productos/sincronizar"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value(1))
                .andExpect(jsonPath("$.mensaje")
                        .value("El método HTTP no está permitido para esta ruta"));
    }
}
