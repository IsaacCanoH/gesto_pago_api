package com.proyecto.servicios.entity.gestopago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gestopago_clientes")
@Getter
@Setter
public class GestoPagoCliente {
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "segundo_nombre", nullable = true, length = 50)
    private String segundoNombre;

    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 50)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "curp", nullable = false, unique = true, length = 18, updatable = false)
    private String curp;

    @Column(name = "rfc", nullable = false, unique = true, length = 13, updatable = false)
    private String rfc;

    @Column(name = "sexo", nullable = false, length = 20)
    private String sexo;

    @Column(name = "nacionalidad", nullable = false, length = 60)
    private String nacionalidad;

    @Column(name = "estado_civil", nullable = false, length = 20)
    private String estadoCivil;

    @Column(name = "telefono_movil", nullable = false, length = 10)
    private String telefonoMovil;

    @Column(name = "telefono_alternativo", length = 10)
    private String telefonoAlternativo;

    @Column(name = "ocupacion", nullable = false, length = 100)
    private String ocupacion;

    @Column(name = "empresa", nullable = false, length = 150)
    private String empresa;

    @Column(name = "ingreso_mensual", nullable = false, precision = 15, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(name = "es_activa", nullable = false)
    private boolean activa = true;

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
