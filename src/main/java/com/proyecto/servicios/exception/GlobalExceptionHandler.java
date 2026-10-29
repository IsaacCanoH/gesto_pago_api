package com.proyecto.servicios.exception;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.proyecto.servicios.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(GestoPagoAuthenticationException.class)
        public ResponseEntity<GenericResponse<Void>> manejarAutenticacion(
                        GestoPagoAuthenticationException exception) {

                log.error("Error de autenticación con GestoPago: {}",
                                exception.getMessage());

                return respuestaError(
                                HttpStatus.UNAUTHORIZED,
                                "No fue posible autenticar la solicitud con GestoPago");
        }

        @ExceptionHandler(GestoPagoCommunicationException.class)
        public ResponseEntity<GenericResponse<Void>> manejarComunicacion(
                        GestoPagoCommunicationException exception) {

                log.error("Error de comunicación con GestoPago: {}",
                                exception.getMessage());

                return respuestaError(
                                HttpStatus.BAD_GATEWAY,
                                "No fue posible comunicarse con GestoPago");
        }

        @ExceptionHandler(GestoPagoExternalResponseException.class)
        public ResponseEntity<GenericResponse<Void>> manejarRespuestaExterna(
                        GestoPagoExternalResponseException exception) {

                log.error("Respuesta no exitosa de GestoPago: {}",
                                exception.getMessage());

                return respuestaError(
                                HttpStatus.BAD_GATEWAY,
                                "GestoPago respondió con un error");
        }

        @ExceptionHandler(GestoPagoSynchronizationInProgressException.class)
        public ResponseEntity<GenericResponse<Void>> manejarSincronizacionEnProceso(
                        GestoPagoSynchronizationInProgressException exception) {

                log.warn("Sincronización de productos rechazada: {}", exception.getMessage());

                return respuestaError(
                                HttpStatus.TOO_MANY_REQUESTS,
                                "Ya existe una sincronización de productos en curso");
        }

        @ExceptionHandler(GestoPagoClienteInvalidoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarClienteInvalido(
                        GestoPagoClienteInvalidoException exception) {

                return respuestaError(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoClienteDuplicadoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarClienteDuplicado(
                        GestoPagoClienteDuplicadoException exception) {

                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoCorreoDuplicadoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarCorreoDuplicado(
                        GestoPagoCorreoDuplicadoException exception) {
                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoCredencialesInvalidasException.class)
        public ResponseEntity<GenericResponse<Void>> manejarCredencialesInvalidas(
                        GestoPagoCredencialesInvalidasException exception) {
                return respuestaError(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoUsuarioInactivoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarUsuarioInactivo(
                        GestoPagoUsuarioInactivoException exception) {
                return respuestaError(HttpStatus.FORBIDDEN, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoUsuarioNoEncontradoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarUsuarioNoEncontrado(
                        GestoPagoUsuarioNoEncontradoException exception) {
                return respuestaError(HttpStatus.NOT_FOUND, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoContrasenaInvalidaException.class)
        public ResponseEntity<GenericResponse<Void>> manejarContrasenaInvalida(
                        GestoPagoContrasenaInvalidaException exception) {
                return respuestaError(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoAccesoDenegadoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarAccesoDenegado(
                        GestoPagoAccesoDenegadoException exception) {
                return respuestaError(HttpStatus.FORBIDDEN, exception.getMessage());
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<GenericResponse<Void>> manejarJsonInvalido(
                        HttpMessageNotReadableException exception) {
                Throwable causa = exception.getCause();
                while (causa != null) {
                        if (causa instanceof InvalidFormatException formato) {
                                String campo = formato.getPath().stream()
                                                .map(JsonMappingException.Reference::getFieldName)
                                                .filter(nombre -> nombre != null && !nombre.isBlank())
                                                .collect(Collectors.joining("."));
                                Class<?> tipo = formato.getTargetType();
                                String detalle = LocalDate.class.equals(tipo)
                                                ? "debe tener formato AAAA-MM-DD"
                                                : Integer.class.equals(tipo) || int.class.equals(tipo)
                                                        ? "debe ser un número entero"
                                                        : Boolean.class.equals(tipo) || boolean.class.equals(tipo)
                                                                ? "debe ser verdadero o falso"
                                                                : "tiene un formato inválido";
                                String mensaje = campo.isEmpty()
                                                ? "El JSON contiene un dato con formato incorrecto"
                                                : LocalDate.class.equals(tipo)
                                                ? "La fecha debe tener formato AAAA-MM-DD"
                                                : "Campo " + campo + ": " + detalle;
                                GenericResponse<Void> response = GenericResponse.error(mensaje);
                                if (!campo.isEmpty()) {
                                        response.setErrores(Map.of(campo, detalle));
                                }
                                return ResponseEntity.badRequest().body(response);
                        }
                        causa = causa.getCause();
                }
                return respuestaError(HttpStatus.BAD_REQUEST,
                                "El JSON es inválido o contiene un dato con formato incorrecto");
        }

        @ExceptionHandler(MissingRequestHeaderException.class)
        public ResponseEntity<GenericResponse<Void>> manejarEncabezadoFaltante(
                        MissingRequestHeaderException exception) {
                return respuestaError(HttpStatus.BAD_REQUEST,
                                "Encabezado " + exception.getHeaderName() + ": es obligatorio");
        }
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<GenericResponse<Void>> manejarRestriccionDeDatos(
                        DataIntegrityViolationException exception) {

                return respuestaError(
                                HttpStatus.CONFLICT,
                                "Los datos entran en conflicto con una restricción de la base de datos");
        }

        @ExceptionHandler(GestoPagoClienteNoEncontradoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarClienteNoEncontrado(
                        GestoPagoClienteNoEncontradoException exception) {

                return respuestaError(HttpStatus.NOT_FOUND, exception.getMessage());
        }

        @ExceptionHandler(PersonaNoEncontradaException.class)
        public ResponseEntity<GenericResponse<Void>> manejarPersonaNoEncontrada(
                        PersonaNoEncontradaException exception) {
                return respuestaError(HttpStatus.NOT_FOUND, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoCuentaNoEncontradaException.class)
        public ResponseEntity<GenericResponse<Void>> manejarCuentaNoEncontrada(
                        GestoPagoCuentaNoEncontradaException exception) {

                return respuestaError(HttpStatus.NOT_FOUND, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoCuentaNoCancelableException.class)
        public ResponseEntity<GenericResponse<Void>> manejarCuentaNoCancelable(
                        GestoPagoCuentaNoCancelableException exception) {
                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
        public ResponseEntity<GenericResponse<Void>> manejarRutaInexistente(Exception exception) {
                return respuestaError(HttpStatus.NOT_FOUND, "La ruta solicitada no existe");
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<GenericResponse<Void>> manejarMetodoNoPermitido(
                        HttpRequestMethodNotSupportedException exception) {
                return respuestaError(HttpStatus.METHOD_NOT_ALLOWED,
                                "El método HTTP no está permitido para esta ruta");
        }

        @ExceptionHandler(GestoPagoPagoInvalidoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarPagoInvalido(
                        GestoPagoPagoInvalidoException exception) {
                return respuestaError(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        @ExceptionHandler({GestoPagoCuentaInactivaException.class, GestoPagoSaldoInsuficienteException.class})
        public ResponseEntity<GenericResponse<Void>> manejarPagoNoDisponible(RuntimeException exception) {
                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoPagoIndeterminadoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarPagoIndeterminado(
                        GestoPagoPagoIndeterminadoException exception) {
                log.warn("El resultado del pago requiere confirmación: {}",
                                exception.getCause() == null ? "respuesta desconocida"
                                                : exception.getCause().getClass().getSimpleName());
                return respuestaError(HttpStatus.BAD_GATEWAY, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoPagoNoEncontradoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarPagoNoEncontrado(
                        GestoPagoPagoNoEncontradoException exception) {
                return respuestaError(HttpStatus.NOT_FOUND, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoSolicitudDuplicadaException.class)
        public ResponseEntity<GenericResponse<Void>> manejarSolicitudDuplicada(
                        GestoPagoSolicitudDuplicadaException exception) {
                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoPinInvalidoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarPinInvalido(GestoPagoPinInvalidoException exception) {
                return respuestaError(HttpStatus.FORBIDDEN, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoPinBloqueadoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarPinBloqueado(GestoPagoPinBloqueadoException exception) {
                return respuestaError(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
        }

        @ExceptionHandler(GestoPagoPinNoConfiguradoException.class)
        public ResponseEntity<GenericResponse<Void>> manejarPinNoConfigurado(GestoPagoPinNoConfiguradoException exception) {
                return respuestaError(HttpStatus.CONFLICT, exception.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<GenericResponse<Void>> manejarErrorNoControlado(
                        Exception exception) {

                log.error("Error no controlado al procesar la solicitud; tipo={}",
                                exception.getClass().getSimpleName());

                return respuestaError(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Ocurrió un error inesperado");
        }

        private ResponseEntity<GenericResponse<Void>> respuestaError(
                        HttpStatus status,
                        String mensaje) {

                GenericResponse<Void> response = GenericResponse.error(mensaje);

                return ResponseEntity.status(status).body(response);
        }
}
