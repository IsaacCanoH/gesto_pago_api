package com.proyecto.servicios.service;

import java.security.SecureRandom;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class GestoPagoUpcGenerator {
    private static final String CARACTERES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int LONGITUD_ALEATORIA = 14;

    private final SecureRandom random = new SecureRandom();

    public String generar() {
        return "P" + random.ints(LONGITUD_ALEATORIA, 0, CARACTERES.length())
                .mapToObj(indice -> String.valueOf(CARACTERES.charAt(indice)))
                .collect(Collectors.joining());
    }
}
