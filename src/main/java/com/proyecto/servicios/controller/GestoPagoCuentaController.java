package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;
import com.proyecto.servicios.security.GestoPagoPrincipal;
import com.proyecto.servicios.service.GestoPagoCuentaService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GestoPagoCuentaController {

    private final GestoPagoCuentaService cuentaService;

    @GetMapping(value = "/cuentas/{numeroCuenta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoCuentaConsultaResponse> consultarCuenta(
            @PathVariable String numeroCuenta) {

        return ResponseEntity.ok(cuentaService.consultarCuenta(numeroCuenta));
    }

    @GetMapping(value = "/cuentas/activas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<GestoPagoCuentaConsultaResponse>> consultarCuentasActivas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        return ResponseEntity.ok(
                new PagedModel<>(cuentaService.consultarCuentasActivas(pagina, tamanio)));
    }

    @DeleteMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<Void> cancelarCuenta(
            @PathVariable String numeroCuenta,
            @AuthenticationPrincipal GestoPagoPrincipal principal) {
        cuentaService.cancelarCuenta(numeroCuenta, principal.usuarioId());
        return ResponseEntity.noContent().build();
    }
}
