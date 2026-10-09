package com.proyecto.servicios.controller;

import java.time.LocalDate;

import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.servicios.model.gestopago.GestoPagoClienteActualizacionRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteParcialRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteResumenResponse;
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
    public ResponseEntity<GestoPagoClienteRegistroResponse> registrarCliente(
            @Valid @RequestBody GestoPagoClienteRegistroRequest request) {

        GestoPagoClienteRegistroResponse response = clienteService.registrarCliente(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/clientes/{id}")
    public ResponseEntity<Void> actualizarCliente(@PathVariable Integer id,
            @Valid @RequestBody GestoPagoClienteActualizacionRequest request) {
        clienteService.actualizarCliente(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/clientes/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> actualizarClienteParcial(
            @PathVariable Integer id,
            @Valid @RequestBody GestoPagoClienteParcialRequest request) {

        clienteService.actualizarClienteParcial(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> desactivarCliente(@PathVariable Integer id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/clientes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoClienteConsultaResponse> consultarCliente(@PathVariable Integer id) {
        return ResponseEntity.ok(consultaService.consultarCliente(id));
    }

    @GetMapping(value = "/clientes/curp/{curp}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoClienteConsultaResponse> consultarClientePorCurp(@PathVariable String curp) {
        return ResponseEntity.ok(consultaService.consultarClientePorCurp(curp));
    }

    @GetMapping(value = "/clientes/rfc/{rfc}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoClienteConsultaResponse> consultarClientePorRfc(@PathVariable String rfc) {
        return ResponseEntity.ok(consultaService.consultarClientePorRfc(rfc));
    }

    @GetMapping(value = "/clientes/cuenta/{numeroCuenta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoClienteConsultaResponse> consultarClientePorNumeroCuenta(
            @PathVariable String numeroCuenta) {

        return ResponseEntity.ok(
                consultaService.consultarClientePorNumeroCuenta(numeroCuenta));
    }

    @GetMapping(value = "/clientes/correo/{correo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GestoPagoClienteConsultaResponse> consultarClientePorCorreo(
            @PathVariable String correo) {

        return ResponseEntity.ok(consultaService.consultarClientePorCorreo(correo));
    }

    @GetMapping(value = "/clientes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<GestoPagoClienteResumenResponse>> consultarClientes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        return ResponseEntity.ok(
                new PagedModel<>(consultaService.consultarClientes(pagina, tamanio)));
    }

    @GetMapping(value = "/clientes/activos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<GestoPagoClienteResumenResponse>> consultarClientesActivos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        return ResponseEntity.ok(
                new PagedModel<>(consultaService.consultarClientesActivos(pagina, tamanio)));
    }

    @GetMapping(value = "/clientes/fecha-creacion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<GestoPagoClienteResumenResponse>> consultarClientesPorFechaCreacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        return ResponseEntity.ok(new PagedModel<>(
                consultaService.consultarClientesPorFechaCreacion(
                        desde, hasta, pagina, tamanio)));
    }
}
