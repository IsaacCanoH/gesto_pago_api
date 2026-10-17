package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoUsuarioConsultaRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping(value = "/usuarios/consultar", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<GestoPagoUsuarioConsultaResponse>> consultarUsuario(
            @Valid @RequestBody GestoPagoUsuarioConsultaRequest request,
            @AuthenticationPrincipal GestoPagoPrincipal principal) {
        return ResponseEntity.ok(GenericResponse.exito("Usuario consultado correctamente",
                usuarioService.consultarUsuario(request.id(), principal.usuarioId())));
    }

    @PutMapping(value = "/usuarios/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<Void>> cambiarContrasena(
            @AuthenticationPrincipal GestoPagoPrincipal principal,
            @Valid @RequestBody GestoPagoCambioContrasenaRequest request) {
        usuarioService.cambiarContrasena(principal.usuarioId(), principal.usuarioId(), request);
        return ResponseEntity.ok(GenericResponse.exito("Contraseña actualizada correctamente", null));
    }
}
