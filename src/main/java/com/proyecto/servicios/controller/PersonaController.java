package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.service.PersonaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PersonaController {
    private final PersonaService personaService;

    @PostMapping(value = "/personas", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<PersonaResponse>> crearPersona(
            @Valid @RequestBody PersonasRequest personasRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.exito(
                "Persona creada correctamente", personaService.creaPersona(personasRequest)));
    }

    @PutMapping(value = "/personas", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<PersonaResponse>> actualizarPersona(
            @Valid @RequestBody PersonasRequest personasRequest) {
        return ResponseEntity.ok(GenericResponse.exito("Persona actualizada correctamente",
                personaService.actualizaPersona(personasRequest)));
    }

    @PostMapping(value = "/personas/eliminar", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<Void>> eliminarPersona(
            @Valid @RequestBody EliminaPersonaRequest request) {
        personaService.eliminaPersona(request);
        return ResponseEntity.ok(GenericResponse.exito("Persona eliminada correctamente", null));
    }
}
