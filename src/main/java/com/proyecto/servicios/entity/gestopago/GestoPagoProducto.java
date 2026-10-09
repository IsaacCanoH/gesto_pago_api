package com.proyecto.servicios.entity.gestopago;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "gestopago_productos")
@Getter
@Setter
public class GestoPagoProducto {
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_producto", nullable = false, unique = true)
    private Long idProducto;

    @Column(name = "id_servicio")
    private Long idServicio;

    @Column(name = "id_cat_tipo_servicio")
    private Long idCatTipoServicio;

    @Column(name = "servicio", nullable = false, length = 255)
    private String servicio;

    @Column(name = "producto", nullable = false, length = 500)
    private String producto;

    @Column(name = "tipo_front")
    private Integer tipoFront;

    @Column(name = "has_digito_verificador")
    private Boolean hasDigitoVerificador;

    @Column(name = "precio", precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(name = "show_ayuda")
    private Boolean showAyuda;

    @Column(name = "tipo_referencia", length = 50)
    private String tipoReferencia;

    @Column(name = "leyenda", columnDefinition = "TEXT")
    private String leyenda;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDate fechaActualizacion;

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
