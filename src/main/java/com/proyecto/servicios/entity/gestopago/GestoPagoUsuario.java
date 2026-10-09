package com.proyecto.servicios.entity.gestopago;

import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gestopago_usuarios")
@Getter
@Setter
public class GestoPagoUsuario {
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private GestoPagoCliente cliente;

    @Column(name = "correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

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
