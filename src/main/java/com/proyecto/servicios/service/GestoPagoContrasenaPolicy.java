package com.proyecto.servicios.service;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.proyecto.servicios.exception.GestoPagoContrasenaInvalidaException;

public final class GestoPagoContrasenaPolicy {
    private static final Pattern FORMATO = Pattern.compile(
            "^(?=.*\\p{Lu})(?=.*\\p{Ll})(?=.*[0-9])(?=.*[^\\p{L}\\p{N}\\s]).+$");

    private GestoPagoContrasenaPolicy() {
    }

    public static void validar(String contrasena) {
        if (contrasena == null || contrasena.length() < 8
                || contrasena.getBytes(StandardCharsets.UTF_8).length > 72
                || !FORMATO.matcher(contrasena).matches()) {
            throw new GestoPagoContrasenaInvalidaException(
                    "La contraseña debe tener entre 8 y 72 bytes e incluir mayúscula, "
                            + "minúscula, número y carácter especial");
        }
    }
}
