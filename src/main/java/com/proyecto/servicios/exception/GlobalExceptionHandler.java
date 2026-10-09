package com.proyecto.servicios.exception;

import java.time.LocalDate;
import java.util.Comparator;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.proyecto.servicios.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

        @ExceptionHandler(GestoPagoClienteInvalidoException.class)
        public ResponseEntity<GenericResponse> manejarClienteInvalido(
                        GestoPagoClienteInvalidoException exception) {

                return respuestaError(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoClienteDuplicadoException.class)
        public ResponseEntity<GenericResponse> manejarClienteDuplicado(
                        GestoPagoClienteDuplicadoException exception) {

                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoCorreoDuplicadoException.class)
        public ResponseEntity<GenericResponse> manejarCorreoDuplicado(
                        GestoPagoCorreoDuplicadoException exception) {
                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoCredencialesInvalidasException.class)
        public ResponseEntity<GenericResponse> manejarCredencialesInvalidas(
                        GestoPagoCredencialesInvalidasException exception) {
                return respuestaError(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoUsuarioInactivoException.class)
        public ResponseEntity<GenericResponse> manejarUsuarioInactivo(
                        GestoPagoUsuarioInactivoException exception) {
                return respuestaError(HttpStatus.FORBIDDEN, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoUsuarioNoEncontradoException.class)
        public ResponseEntity<GenericResponse> manejarUsuarioNoEncontrado(
                        GestoPagoUsuarioNoEncontradoException exception) {
                return respuestaError(HttpStatus.NOT_FOUND, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoContrasenaInvalidaException.class)
        public ResponseEntity<GenericResponse> manejarContrasenaInvalida(
                        GestoPagoContrasenaInvalidaException exception) {
                return respuestaError(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoAccesoDenegadoException.class)
        public ResponseEntity<GenericResponse> manejarAccesoDenegado(
                        GestoPagoAccesoDenegadoException exception) {
                return respuestaError(HttpStatus.FORBIDDEN, exception.getMessage());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<GenericResponse> manejarCamposInvalidos(
                        MethodArgumentNotValidException exception) {

                String mensaje = exception.getBindingResult().getFieldErrors().stream()
                                .min(Comparator.comparingInt(this::prioridadError))
                                .map(error -> "Campo " + error.getField()
                                                + ": " + error.getDefaultMessage())
                                .orElse("Los datos enviados no son válidos");

                return respuestaError(HttpStatus.BAD_REQUEST, mensaje);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<GenericResponse> manejarJsonInvalido(
                        HttpMessageNotReadableException exception) {
                Throwable causa = exception.getCause();
                while (causa != null) {
                        if (causa instanceof InvalidFormatException formato
                                        && LocalDate.class.equals(formato.getTargetType())) {
                                return respuestaError(HttpStatus.BAD_REQUEST,
                                                "La fecha debe tener formato AAAA-MM-DD");
                        }
                        causa = causa.getCause();
                }
                return respuestaError(HttpStatus.BAD_REQUEST,
                                "El JSON es inválido o contiene un dato con formato incorrecto");
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<GenericResponse> manejarParametroInvalido(
                        MethodArgumentTypeMismatchException exception) {
                Class<?> tipo = exception.getRequiredType();
                String detalle = LocalDate.class.equals(tipo)
                                ? "debe tener formato AAAA-MM-DD"
                                : (Integer.class.equals(tipo) || int.class.equals(tipo))
                                                ? "debe ser un número entero"
                                                : "tiene un formato inválido";
                return respuestaError(HttpStatus.BAD_REQUEST,
                                "Parámetro " + exception.getName() + ": " + detalle);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<GenericResponse> manejarParametroFaltante(
                        MissingServletRequestParameterException exception) {
                return respuestaError(HttpStatus.BAD_REQUEST,
                                "Parámetro " + exception.getParameterName() + ": es obligatorio");
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<GenericResponse> manejarRestriccionDeDatos(
                        DataIntegrityViolationException exception) {

                return respuestaError(
                                HttpStatus.CONFLICT,
                                "Los datos entran en conflicto con una restricción de la base de datos");
        }

        @ExceptionHandler(GestoPagoClienteNoEncontradoException.class)
        public ResponseEntity<GenericResponse> manejarClienteNoEncontrado(
                        GestoPagoClienteNoEncontradoException exception) {

                return respuestaError(HttpStatus.NOT_FOUND, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoCuentaNoEncontradaException.class)
        public ResponseEntity<GenericResponse> manejarCuentaNoEncontrada(
                        GestoPagoCuentaNoEncontradaException exception) {

                return respuestaError(HttpStatus.NOT_FOUND, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoCuentaNoCancelableException.class)
        public ResponseEntity<GenericResponse> manejarCuentaNoCancelable(
                        GestoPagoCuentaNoCancelableException exception) {
                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<GenericResponse> manejarErrorNoControlado(
                        Exception exception) {

                log.error("Error no controlado al procesar la solicitud");

                return respuestaError(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Ocurrió un error inesperado");
        }

        private ResponseEntity<GenericResponse> respuestaError(
                        HttpStatus status,
                        String mensaje) {

                GenericResponse response = new GenericResponse();
                response.setCodigo(1);
                response.setMensaje(mensaje);

                return ResponseEntity.status(status).body(response);
        }

        private int prioridadError(FieldError error) {
                return "NotBlank".equals(error.getCode()) || "NotNull".equals(error.getCode())
                                ? 0 : 1;
        }
}
