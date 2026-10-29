package com.proyecto.servicios.exception;

public class GestoPagoSaldoInsuficienteException extends RuntimeException {
    public GestoPagoSaldoInsuficienteException() {
        super("Saldo disponible insuficiente para realizar el pago");
    }
}
