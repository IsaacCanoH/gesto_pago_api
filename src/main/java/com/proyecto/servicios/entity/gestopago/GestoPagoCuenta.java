package com.proyecto.servicios.entity.gestopago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gestopago_cuentas")
@Getter
@Setter
public class GestoPagoCuenta {
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private GestoPagoCliente cliente;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20, updatable = false)
    private String numeroCuenta;

    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(name = "estatus", nullable = false, length = 10)
    private String estatus = "ACTIVA";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDate fechaActualizacion;

    @PrePersist
    void alCrear() {
        LocalDate hoy = LocalDate.now(ZONA);
        fechaCreacion = hoy;
        fechaActualizacion = hoy;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = LocalDate.now(ZONA);
    }
}
