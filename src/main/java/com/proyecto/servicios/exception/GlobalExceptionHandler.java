package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(GestoPagoAuthenticationException.class)
    public ResponseEntity<GenericResponse> manejarAutenticacion(
            GestoPagoAuthenticationException exception) {

        log.error("Error de autenticación con GestoPago: {}",
                exception.getMessage());

        return respuestaError(
                HttpStatus.UNAUTHORIZED,
                "No fue posible autenticar la solicitud con GestoPago");
    }

    @ExceptionHandler(GestoPagoCommunicationException.class)
    public ResponseEntity<GenericResponse> manejarComunicacion(
            GestoPagoCommunicationException exception) {

        log.error("Error de comunicación con GestoPago: {}",
                exception.getMessage());

        return respuestaError(
                HttpStatus.BAD_GATEWAY,
                "No fue posible comunicarse con GestoPago");
    }

    @ExceptionHandler(GestoPagoExternalResponseException.class)
    public ResponseEntity<GenericResponse> manejarRespuestaExterna(
            GestoPagoExternalResponseException exception) {

        log.error("Respuesta no exitosa de GestoPago: {}",
                exception.getMessage());

        return respuestaError(
                HttpStatus.BAD_GATEWAY,
                "GestoPago respondió con un error");
    }

    @ExceptionHandler(GestoPagoSynchronizationInProgressException.class)
    public ResponseEntity<GenericResponse> manejarSincronizacionEnProceso(
            GestoPagoSynchronizationInProgressException exception) {

        log.warn("Sincronización de productos rechazada: {}", exception.getMessage());

        return respuestaError(
                HttpStatus.TOO_MANY_REQUESTS,
                "Ya existe una sincronización de productos en curso");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> manejarErrorNoControlado(
            Exception exception) {

        log.error("Error no controlado durante la sincronización de productos");

        return respuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error al sincronizar productos");
    }

    private ResponseEntity<GenericResponse> respuestaError(
            HttpStatus status,
            String mensaje) {

        GenericResponse response = new GenericResponse();
        response.setCodigo(1);
        response.setMensaje(mensaje);

        return ResponseEntity.status(status).body(response);
    }
}