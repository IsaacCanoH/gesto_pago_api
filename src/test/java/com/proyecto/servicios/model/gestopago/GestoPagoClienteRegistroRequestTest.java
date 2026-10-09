package com.proyecto.servicios.model.gestopago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

class GestoPagoClienteRegistroRequestTest {
    private static final ValidatorFactory VALIDACION = Validation.buildDefaultValidatorFactory();

    @AfterAll
    static void cerrarValidador() {
        VALIDACION.close();
    }

    @Test
    void camposObligatorios() {
        var errores = VALIDACION.getValidator()
                .validate(new GestoPagoClienteRegistroRequest()).stream()
                .map(error -> error.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(errores.containsAll(Set.of(
                "nombre", "apellidoPaterno", "apellidoMaterno", "fechaNacimiento",
                "curp", "rfc", "correo", "contrasena", "telefonoMovil",
                "sexo", "nacionalidad", "estadoCivil", "ocupacion", "empresa",
                "ingresoMensual", "domicilio")));
    }

    @Test
    void camposDeDomicilio() {
        var errores = VALIDACION.getValidator()
                .validate(new GestoPagoDomicilioRequest()).stream()
                .map(error -> error.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(errores.containsAll(Set.of(
                "calle", "numeroExterior", "colonia", "municipio",
                "estado", "codigoPostal", "pais")));
        assertFalse(errores.contains("numeroInterior"));
    }

    @Test
    void nombresYApellidos() {
        var request = new GestoPagoClienteRegistroRequest();
        request.setNombre("A1");
        request.setApellidoPaterno("L");
        request.setApellidoMaterno("   ");

        assertFalse(valido(request, "nombre"));
        assertFalse(valido(request, "apellidoPaterno"));
        assertFalse(valido(request, "apellidoMaterno"));

        request.setNombre("A".repeat(51));
        assertFalse(valido(request, "nombre"));

        request.setNombre("María José");
        request.setApellidoPaterno("López");
        request.setApellidoMaterno("García");
        assertTrue(valido(request, "nombre"));
        assertTrue(valido(request, "apellidoPaterno"));
        assertTrue(valido(request, "apellidoMaterno"));
        assertTrue(valido(request, "segundoNombre"));
    }

    @Test
    void curpYRfc() {
        var request = new GestoPagoClienteRegistroRequest();
        request.setCurp("CANO050429HKNTEW0");
        request.setRfc("LOGM950814A1");
        assertFalse(valido(request, "curp"));
        assertFalse(valido(request, "rfc"));

        request.setCurp("CANO050429HKNTEW00");
        request.setRfc("LOGM950814A1B");
        assertTrue(valido(request, "curp"));
        assertTrue(valido(request, "rfc"));
    }

    @Test
    void correoYTelefonos() {
        var request = new GestoPagoClienteRegistroRequest();
        request.setCorreo("correo-invalido");
        request.setTelefonoMovil("123456789");
        request.setTelefonoAlternativo("abc");
        assertFalse(valido(request, "correo"));
        assertFalse(valido(request, "telefonoMovil"));
        assertFalse(valido(request, "telefonoAlternativo"));

        request.setCorreo("cliente@example.com");
        request.setTelefonoMovil("5512345678");
        request.setTelefonoAlternativo(null);
        assertTrue(valido(request, "correo"));
        assertTrue(valido(request, "telefonoMovil"));
        assertTrue(valido(request, "telefonoAlternativo"));

        request.setCorreo("a".repeat(95) + "@x.com");
        assertFalse(valido(request, "correo"));
    }

    @Test
    void fechaEIngreso() {
        var request = new GestoPagoClienteRegistroRequest();
        request.setFechaNacimiento(LocalDate.now().plusDays(1));
        request.setIngresoMensual(BigDecimal.ZERO);
        assertFalse(valido(request, "fechaNacimiento"));
        assertFalse(valido(request, "ingresoMensual"));

        request.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        request.setIngresoMensual(new BigDecimal("0.01"));
        assertTrue(valido(request, "fechaNacimiento"));
        assertTrue(valido(request, "ingresoMensual"));
    }

    @Test
    void domicilioYContrasena() {
        var request = new GestoPagoClienteRegistroRequest();
        request.setContrasena("sinclave");
        assertFalse(valido(request, "domicilio"));
        assertFalse(valido(request, "contrasena"));

        var domicilio = new GestoPagoDomicilioRequest();
        domicilio.setCodigoPostal("123");
        assertFalse(VALIDACION.getValidator().validateProperty(domicilio, "codigoPostal").isEmpty());
        domicilio.setCodigoPostal("37803");
        assertTrue(VALIDACION.getValidator().validateProperty(domicilio, "codigoPostal").isEmpty());

        request.setContrasena("ClaveSegura1!");
        assertTrue(valido(request, "contrasena"));
    }

    @Test
    void contrasenasDebiles() {
        var request = new GestoPagoClienteRegistroRequest();

        for (String contrasena : List.of(
                "Corta1!", "minusculas1!", "MAYUSCULAS1!", "SinNumero!", "SinSimbolo1")) {
            request.setContrasena(contrasena);
            assertFalse(valido(request, "contrasena"), contrasena);
        }
    }

    @Test
    void mensajeDeRfc() {
        var request = new GestoPagoClienteRegistroRequest();
        request.setRfc("LOG950814A12");

        var errores = VALIDACION.getValidator().validateProperty(request, "rfc");

        assertEquals("debe tener 13 caracteres: 4 letras, 6 dígitos y 3 alfanuméricos",
                errores.iterator().next().getMessage());
    }

    private boolean valido(GestoPagoClienteRegistroRequest request, String campo) {
        return VALIDACION.getValidator().validateProperty(request, campo).isEmpty();
    }
}
