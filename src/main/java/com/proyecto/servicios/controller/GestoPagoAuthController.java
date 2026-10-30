package com.proyecto.servicios.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.servicios.model.gestopago.GestoPagoLoginRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginResponse;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.service.GestoPagoAuthService;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GestoPagoAuthController {
    private final GestoPagoAuthService authService;

    @PostMapping(value = "/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirements
    public ResponseEntity<GenericResponse<GestoPagoLoginResponse>> iniciarSesion(
            @Valid @RequestBody GestoPagoLoginRequest request) {
        return ResponseEntity.ok(GenericResponse.exito("Inicio de sesión correcto",
                authService.iniciarSesion(request)));
    }
}
