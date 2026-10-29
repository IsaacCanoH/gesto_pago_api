package com.proyecto.servicios.entity.gestopago;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gestopago_pagos")
@Getter
@Setter
public class GestoPagoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false, updatable = false)
    private GestoPagoCuenta cuenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, updatable = false)
    private GestoPagoProducto producto;

    @Column(name = "id_servicio_proveedor", nullable = false, updatable = false)
    private Long idServicioProveedor;

    @Column(name = "id_producto_proveedor", nullable = false, updatable = false)
    private Long idProductoProveedor;

    @Column(name = "monto_pago", nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal montoPago;

    @Column(name = "telefono", nullable = false, length = 20, updatable = false)
    private String telefono;

    @Column(name = "referencia", length = 100, updatable = false)
    private String referencia;

    @Column(name = "upc", nullable = false, unique = true, length = 15, updatable = false)
    private String upc;

    @Column(name = "solicitud_id", unique = true, updatable = false)
    private UUID solicitudId;

    @Column(name = "hora_local", nullable = false, length = 14, updatable = false)
    private String horaLocal;

    @Column(name = "unidad", nullable = false, length = 25, updatable = false)
    private String unidad;

    @Column(name = "random_proveedor", length = 64)
    private String randomProveedor;

    @Column(name = "signed_proveedor", columnDefinition = "TEXT")
    private String signedProveedor;

    @Column(name = "fecha_envio")
    private Instant fechaEnvio;

    @Column(name = "fecha_ultima_confirmacion")
    private Instant fechaUltimaConfirmacion;

    @Column(name = "codigo_proveedor", length = 10)
    private String codigoProveedor;

    @Column(name = "mensaje_proveedor", length = 500)
    private String mensajeProveedor;

    @Column(name = "id_transaccion_proveedor", length = 60)
    private String idTransaccionProveedor;

    @Column(name = "numero_autorizacion", length = 60)
    private String numeroAutorizacion;

    @Column(name = "fecha_resolucion")
    private Instant fechaResolucion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false, length = 10)
    private GestoPagoEstadoPago estatus = GestoPagoEstadoPago.PENDIENTE;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    @PrePersist
    void alCrear() {
        Instant ahora = Instant.now();
        fechaCreacion = ahora;
        fechaActualizacion = ahora;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = Instant.now();
    }
}
