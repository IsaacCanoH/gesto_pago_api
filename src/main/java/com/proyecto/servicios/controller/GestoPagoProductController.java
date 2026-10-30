package com.proyecto.servicios.controller;

import java.util.List;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoPaginaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoSincronizacionResponse;
import com.proyecto.servicios.service.GestoPagoProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GestoPagoProductController {

    private final GestoPagoProductService gestoPagoProductService;

    @PostMapping(value = "/gestopago/productos/sincronizar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<GestoPagoSincronizacionResponse>> sincronizarProductos() {
        int productosSincronizados = gestoPagoProductService.sincronizarProductos();
        String mensaje = productosSincronizados == 0
                ? "No hubo productos para sincronizar"
                : "Productos sincronizados correctamente";

        return ResponseEntity.ok(GenericResponse.exito(mensaje,
                new GestoPagoSincronizacionResponse(productosSincronizados)));
    }

    @GetMapping(value = "/gestopago/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<List<GestoPagoProductoResponse>>> consultarProductos() {
        return ResponseEntity.ok(GenericResponse.exito("Productos consultados correctamente",
                gestoPagoProductService.consultarProductos()));
    }

    @PostMapping(value = "/gestopago/productos/paginar", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<PagedModel<GestoPagoProductoResponse>>> consultarProductosPaginados(
            @Valid @RequestBody GestoPagoProductoPaginaRequest request) {
        var pagina = new PagedModel<>(gestoPagoProductService.consultarProductosPaginados(request));
        return ResponseEntity.ok(GenericResponse.exito("Consulta de productos realizada", pagina));
    }
}