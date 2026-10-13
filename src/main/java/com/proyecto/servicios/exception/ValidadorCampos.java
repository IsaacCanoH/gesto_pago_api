package com.proyecto.servicios.exception;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.proyecto.servicios.model.GenericResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidadorCampos {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> validarCampos(MethodArgumentNotValidException exception) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField)
                        .thenComparingInt(error ->
                                "NotBlank".equals(error.getCode()) || "NotNull".equals(error.getCode()) ? 0 : 1))
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        String detalle = errores.entrySet().stream()
                .map(error -> error.getKey() + ": " + error.getValue())
                .collect(Collectors.joining("; "));

        GenericResponse response = new GenericResponse();
        response.setCodigo(1);
        response.setMensaje(detalle.isEmpty()
                ? "Los datos enviados no son válidos"
                : "Datos no válidos: " + detalle);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
