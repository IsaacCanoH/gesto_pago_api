package com.proyecto.servicios.model.gestopago;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@XmlRootElement(name = "RESPONSE")
@XmlAccessorType(XmlAccessType.FIELD)
public class GestoPagoProductListResponse {

    @XmlElement(name = "MENSAJE")
    private Mensaje mensaje;

    @XmlElementWrapper(name = "PRODUCTOS")
    @XmlElement(name = "producto")
    private List<Producto> productos = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Mensaje {

        @XmlElement(name = "CODIGO")
        private String codigo;

        @XmlElement(name = "TEXTO")
        private String texto;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Producto {

        @XmlAttribute
        private String servicio;

        @XmlAttribute
        private String producto;

        @XmlAttribute
        private Long idServicio;

        @XmlAttribute
        private Long idProducto;

        @XmlAttribute
        private Long idCatTipoServicio;

        @XmlAttribute
        private Integer tipoFront;

        @XmlAttribute
        private Boolean hasDigitoVerificador;

        @XmlAttribute
        private BigDecimal precio;

        @XmlAttribute
        private Boolean showAyuda;

        @XmlAttribute
        private String tipoReferencia;

        @XmlElement(name = "legend")
        private String legend;
    }
}