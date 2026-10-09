package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class GestoPagoClienteConsultaResponse {
    private Integer id;
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String curp;
    private String rfc;
    private String sexo;
    private String nacionalidad;
    private String estadoCivil;
    private String correo;
    private String telefonoMovil;
    private String telefonoAlternativo;
    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;
    private boolean activa;
    private LocalDate fechaCreacion;
    private LocalDate fechaActualizacion;
    private GestoPagoDomicilioConsultaResponse domicilio;
    private List<GestoPagoCuentaConsultaResponse> cuentas;
}
