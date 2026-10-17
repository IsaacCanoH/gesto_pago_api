package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaBusquedaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaCancelacionRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;
import com.proyecto.servicios.service.GestoPagoCuentaService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class GestoPagoCuentaController {

    private final GestoPagoCuentaService cuentaService;

    @PostMapping(value = "/cuentas/buscar", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<PagedModel<GestoPagoCuentaConsultaResponse>>> buscarCuentas(
            @Valid @RequestBody GestoPagoCuentaBusquedaRequest request) {
        var pagina = new PagedModel<>(cuentaService.buscarCuentas(request));
        return ResponseEntity.ok(GenericResponse.exito("Consulta de cuentas realizada", pagina));
    }

    @PostMapping(value = "/cuentas/cancelar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<Void>> cancelarCuenta(
            @Valid @RequestBody GestoPagoCuentaCancelacionRequest request) {
        cuentaService.cancelarCuenta(request.numeroCuenta(), null);
        return ResponseEntity.ok(GenericResponse.exito("Cuenta cancelada correctamente", null));
    }
}
