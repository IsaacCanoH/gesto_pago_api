package com.proyecto.servicios.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.model.GenericResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GestoPagoSecurityErrorWriter {
    private final ObjectMapper objectMapper;

    public void escribir(HttpServletResponse response, HttpStatus status, String mensaje)
            throws IOException {
        GenericResponse body = new GenericResponse();
        body.setCodigo(1);
        body.setMensaje(mensaje);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
