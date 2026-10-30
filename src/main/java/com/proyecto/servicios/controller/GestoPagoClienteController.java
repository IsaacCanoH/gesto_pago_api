package com.proyecto.servicios.controller;

import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteActualizacionRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteBusquedaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteBusquedaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteIdRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteParcialRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroResponse;
import com.proyecto.servicios.service.GestoPagoClienteConsultaService;
import com.proyecto.servicios.service.GestoPagoClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GestoPagoClienteController {

    private final GestoPagoClienteService clienteService;
    private final GestoPagoClienteConsultaService consultaService;

    @PostMapping(value = "/clientes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<GestoPagoClienteRegistroResponse>> registrarCliente(
            @Valid @RequestBody GestoPagoClienteRegistroRequest request) {

        GestoPagoClienteRegistroResponse response = clienteService.registrarCliente(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenericResponse.exito("Cliente registrado correctamente", response));
    }

    @PutMapping(value = "/clientes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<Void>> actualizarCliente(
            @Valid @RequestBody GestoPagoClienteActualizacionRequest request) {
        clienteService.actualizarCliente(request.getId(), request);
        return ResponseEntity.ok(GenericResponse.exito("Cliente actualizado correctamente", null));
    }

    @PatchMapping(value = "/clientes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<Void>> actualizarClienteParcial(
            @Valid @RequestBody GestoPagoClienteParcialRequest request) {
        clienteService.actualizarClienteParcial(request.getId(), request);
        return ResponseEntity.ok(GenericResponse.exito("Cliente actualizado correctamente", null));
    }

    @PostMapping(value = "/clientes/desactivar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<Void>> desactivarCliente(
            @Valid @RequestBody GestoPagoClienteIdRequest request) {
        clienteService.desactivarCliente(request.id());
        return ResponseEntity.ok(GenericResponse.exito("Cliente desactivado correctamente", null));
    }

    @PostMapping(value = "/clientes/buscar", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<GestoPagoClienteBusquedaResponse>> buscarClientes(
            @Valid @RequestBody GestoPagoClienteBusquedaRequest request) {
        var resultado = consultaService.buscarClientes(request);
        var datos = new GestoPagoClienteBusquedaResponse(
                new PagedModel<>(resultado.resultados()), resultado.detalle());
        return ResponseEntity.ok(GenericResponse.exito("Consulta de clientes realizada", datos));
    }
}
