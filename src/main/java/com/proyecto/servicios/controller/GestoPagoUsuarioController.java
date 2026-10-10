package com.proyecto.servicios.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.servicios.model.gestopago.GestoPagoCambioContrasenaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoUsuarioConsultaResponse;
import com.proyecto.servicios.security.GestoPagoPrincipal;
import com.proyecto.servicios.service.GestoPagoUsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GestoPagoUsuarioController {
    private final GestoPagoUsuarioService usuarioService;

    @GetMapping(value = "/usuarios/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoUsuarioConsultaResponse> consultarUsuario(
            @PathVariable Integer id, @AuthenticationPrincipal GestoPagoPrincipal principal) {
        return ResponseEntity.ok(usuarioService.consultarUsuario(
                id, principal == null ? null : principal.usuarioId()));
    }

    @PutMapping(value = "/usuarios/{id}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cambiarContrasena(
            @PathVariable Integer id,
            @AuthenticationPrincipal GestoPagoPrincipal principal,
            @Valid @RequestBody GestoPagoCambioContrasenaRequest request) {
        usuarioService.cambiarContrasena(
                id, principal == null ? null : principal.usuarioId(), request);
        return ResponseEntity.noContent().build();
    }
}
