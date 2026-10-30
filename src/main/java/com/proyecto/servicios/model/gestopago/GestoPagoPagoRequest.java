package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GestoPagoPagoRequest(
        @NotNull UUID solicitudId,
        @NotBlank @Size(min = 20, max = 20) String numeroCuenta,
        @NotNull @Positive Long idServicio,
        @NotNull @Positive Long idProducto,
        @NotNull @Positive Integer tipoFront,
        @JsonAlias("monto") @NotNull @DecimalMin("0.01") BigDecimal montoPago,
        String referencia,
        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = "^[0-9]{6}$", message = "debe tener exactamente 6 dígitos") String pin) {

    @Override
    public String toString() {
        return "GestoPagoPagoRequest[protegido]";
    }

    public GestoPagoReservaPagoCommand aComando() {
        boolean pagoTelefonico = Integer.valueOf(1).equals(tipoFront);
        return new GestoPagoReservaPagoCommand(
                numeroCuenta, idServicio, idProducto, tipoFront, montoPago,
                pagoTelefonico ? referencia : null,
                pagoTelefonico ? null : referencia,
                solicitudId);
    }
}
