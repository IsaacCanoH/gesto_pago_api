package com.proyecto.servicios.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoPagoConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoPagoRequest;
import com.proyecto.servicios.security.GestoPagoPrincipal;
import com.proyecto.servicios.service.GestoPagoEstadoPagoService;
import com.proyecto.servicios.service.GestoPagoPagoWorkflowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GestoPagoPagoController {
    private final GestoPagoEstadoPagoService estadoService;
    private final GestoPagoPagoWorkflowService workflowService;

    @PostMapping(value = "/pagos", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoPagoConsultaResponse> iniciar(
            @AuthenticationPrincipal GestoPagoPrincipal principal,
            @RequestHeader("Idempotency-Key") UUID solicitudId,
            @Valid @RequestBody GestoPagoPagoRequest request) {
        return ResponseEntity.ok(workflowService.iniciar(
                request.aComando(solicitudId), principal.usuarioId(), request.pin()));
    }

    @PostMapping(value = "/pagos/consultar", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoPagoConsultaResponse> consultar(
            @AuthenticationPrincipal GestoPagoPrincipal principal,
            @Valid @RequestBody GestoPagoPagoConsultaRequest request) {
        return ResponseEntity.ok(estadoService.consultar(request.upc(), principal.usuarioId()));
    }
}
