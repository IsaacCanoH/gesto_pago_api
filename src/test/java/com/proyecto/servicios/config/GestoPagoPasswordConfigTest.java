package com.proyecto.servicios.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class GestoPagoPasswordConfigTest {

    @Test
    void generaHashBCrypt() {
        PasswordEncoder encoder = new GestoPagoPasswordConfig().passwordEncoder();
        String contrasena = "ClaveSegura1!";

        String hash = encoder.encode(contrasena);

        assertNotEquals(contrasena, hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches(contrasena, hash));
    }
}
