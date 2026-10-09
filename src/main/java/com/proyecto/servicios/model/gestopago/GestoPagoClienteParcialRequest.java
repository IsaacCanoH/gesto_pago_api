package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GestoPagoClienteParcialRequest implements TextoRecortable {

    @Size(min = 2, max = 50, message = "debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L} ]+$",
            message = "solo admite letras y espacios, no solo espacios")
    private String nombre;

    @Size(min = 2, max = 50, message = "debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L} ]+$",
            message = "solo admite letras y espacios, no solo espacios")
    private String segundoNombre;

    @Size(min = 2, max = 50, message = "debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L} ]+$",
            message = "solo admite letras y espacios, no solo espacios")
    private String apellidoPaterno;

    @Size(min = 2, max = 50, message = "debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L} ]+$",
            message = "solo admite letras y espacios, no solo espacios")
    private String apellidoMaterno;

    @Past(message = "debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @Email(message = "debe tener un formato válido")
    @Size(min = 1, max = 100, message = "debe tener entre 1 y 100 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String correo;

    @Pattern(regexp = "^[0-9]{10}$", message = "debe tener exactamente 10 dígitos")
    private String telefonoMovil;

    @Pattern(regexp = "^[0-9]{10}$", message = "debe tener exactamente 10 dígitos")
    private String telefonoAlternativo;

    @Size(min = 1, max = 20, message = "debe tener entre 1 y 20 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String sexo;

    @Size(min = 1, max = 60, message = "debe tener entre 1 y 60 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String nacionalidad;

    @Size(min = 1, max = 20, message = "debe tener entre 1 y 20 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String estadoCivil;

    @Size(min = 1, max = 100, message = "debe tener entre 1 y 100 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String ocupacion;

    @Size(min = 1, max = 150, message = "debe tener entre 1 y 150 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String empresa;

    @DecimalMin(value = "0.01", message = "debe ser mayor que cero")
    @Digits(integer = 13, fraction = 2, message = "admite hasta 13 enteros y 2 decimales")
    private BigDecimal ingresoMensual;

    @Valid
    private GestoPagoDomicilioParcialRequest domicilio;

    @Override
    public void recortarEspacios() {
        nombre = TextoRecortable.trim(nombre);
        segundoNombre = TextoRecortable.trimOpcional(segundoNombre);
        apellidoPaterno = TextoRecortable.trim(apellidoPaterno);
        apellidoMaterno = TextoRecortable.trim(apellidoMaterno);
        correo = TextoRecortable.trim(correo);
        telefonoMovil = TextoRecortable.trim(telefonoMovil);
        telefonoAlternativo = TextoRecortable.trimOpcional(telefonoAlternativo);
        sexo = TextoRecortable.trim(sexo);
        nacionalidad = TextoRecortable.trim(nacionalidad);
        estadoCivil = TextoRecortable.trim(estadoCivil);
        ocupacion = TextoRecortable.trim(ocupacion);
        empresa = TextoRecortable.trim(empresa);
        if (domicilio != null) {
            domicilio.recortarEspacios();
        }
    }
}
