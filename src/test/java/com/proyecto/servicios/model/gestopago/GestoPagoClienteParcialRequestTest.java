package com.proyecto.servicios.model.gestopago;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class GestoPagoClienteParcialRequestTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void distingueCamposOmitidosDeNullExplicito() throws Exception {
        var omitidos = mapper.readValue("{\"id\":7,\"domicilio\":{}}",
                GestoPagoClienteParcialRequest.class);
        var nulos = mapper.readValue("""
                {"id":7,"segundoNombre":null,"telefonoAlternativo":null,
                 "domicilio":{"numeroInterior":null}}
                """, GestoPagoClienteParcialRequest.class);

        assertFalse(omitidos.debeLimpiarSegundoNombre());
        assertFalse(omitidos.debeLimpiarTelefonoAlternativo());
        assertFalse(omitidos.getDomicilio().debeLimpiarNumeroInterior());
        assertTrue(nulos.debeLimpiarSegundoNombre());
        assertTrue(nulos.debeLimpiarTelefonoAlternativo());
        assertTrue(nulos.getDomicilio().debeLimpiarNumeroInterior());
    }

    @Test
    void unValorEnviadoNoSeConsideraBorrado() throws Exception {
        var request = mapper.readValue("""
                {"id":7,"segundoNombre":"Luis","telefonoAlternativo":"5512345678",
                 "domicilio":{"numeroInterior":"2"}}
                """, GestoPagoClienteParcialRequest.class);

        assertFalse(request.debeLimpiarSegundoNombre());
        assertFalse(request.debeLimpiarTelefonoAlternativo());
        assertFalse(request.getDomicilio().debeLimpiarNumeroInterior());
    }
}
