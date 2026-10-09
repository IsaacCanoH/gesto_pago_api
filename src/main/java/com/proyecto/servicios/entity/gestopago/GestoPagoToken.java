package com.proyecto.servicios.entity.gestopago;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "gestopago_tokens")
@Getter
@Setter
public class GestoPagoToken {
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_distribuidor", nullable = false)
    private Integer idDistribuidor;

    @Column(name = "codigo_dispositivo", nullable = false, length = 100)
    private String codigoDispositivo;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "token_type", length = 50)
    private String tokenType;

    @Column(name = "expires_in")
    private Long expiresIn;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDate fechaActualizacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    void onCreate() {
        LocalDate hoy = LocalDate.now(ZONA);
        fechaCreacion = hoy;
        fechaActualizacion = hoy;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDate.now(ZONA);
    }
}
