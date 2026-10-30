package com.proyecto.servicios.exception;

public class PersonaNoEncontradaException extends RuntimeException {
    public PersonaNoEncontradaException() {
        super("La persona no existe");
    }
}
