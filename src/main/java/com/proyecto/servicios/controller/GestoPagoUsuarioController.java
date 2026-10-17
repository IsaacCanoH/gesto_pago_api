package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoUsuarioConsultaRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.servicios.model.gestopago.GestoPagoCambioContrasenaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoUsuarioConsultaResponse;
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
            @Valid @RequestBody GestoPagoUsuarioConsultaRequest request) {
        return ResponseEntity.ok(GenericResponse.exito("Usuario consultado correctamente",
                usuarioService.consultarUsuario(request.id(), null)));
    }

    @PutMapping(value = "/usuarios/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<Void>> cambiarContrasena(
            @Valid @RequestBody GestoPagoCambioContrasenaRequest request) {
        usuarioService.cambiarContrasena(request.id(), null, request);
        return ResponseEntity.ok(GenericResponse.exito("Contraseña actualizada correctamente", null));
    }
}
