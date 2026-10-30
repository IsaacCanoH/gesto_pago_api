package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.service.GestoPagoProductService;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GestoPagoProductController {

    private final GestoPagoProductService gestoPagoProductService;

    @PostMapping(value = "/gestopago/productos/sincronizar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse> sincronizarProductos() {
        int productosSincronizados = gestoPagoProductService.sincronizarProductos();

        GenericResponse response = new GenericResponse();
        response.setCodigo(0);
        response.setMensaje("Productos sincronizados: " + productosSincronizados);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/gestopago/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GestoPagoProductoResponse>> consultarProductos() {
        return ResponseEntity.ok(gestoPagoProductService.consultarProductos());
    }
}
