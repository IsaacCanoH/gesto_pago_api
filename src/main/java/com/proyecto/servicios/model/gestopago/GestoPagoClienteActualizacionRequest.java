package com.proyecto.servicios.model.gestopago;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GestoPagoClienteActualizacionRequest implements TextoRecortable {

    @NotBlank(message = "es obligatorio")
    @Size(min = 2, max = 50, message = "debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "solo admite letras y espacios")
    private String nombre;

    @Size(min = 2, max = 50, message = "debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "solo admite letras y espacios")
    private String segundoNombre;

    @NotBlank(message = "es obligatorio")
    @Size(min = 2, max = 50, message = "debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "solo admite letras y espacios")
    private String apellidoPaterno;

    @NotBlank(message = "es obligatorio")
    @Size(min = 2, max = 50, message = "debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "solo admite letras y espacios")
    private String apellidoMaterno;

    @NotNull(message = "es obligatoria")
    @Past(message = "debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "es obligatorio")
    @Email(message = "debe tener un formato válido")
    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String correo;

    @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "debe tener exactamente 10 dígitos")
    private String telefonoMovil;

    @Pattern(regexp = "^[0-9]{10}$", message = "debe tener exactamente 10 dígitos")
    private String telefonoAlternativo;

    @NotBlank(message = "es obligatorio")
    @Size(max = 20, message = "no debe superar 20 caracteres")
    private String sexo;

    @NotBlank(message = "es obligatoria")
    @Size(max = 60, message = "no debe superar 60 caracteres")
    private String nacionalidad;

    @NotBlank(message = "es obligatorio")
    @Size(max = 20, message = "no debe superar 20 caracteres")
    private String estadoCivil;

    @NotBlank(message = "es obligatoria")
    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String ocupacion;

    @NotBlank(message = "es obligatoria")
    @Size(max = 150, message = "no debe superar 150 caracteres")
    private String empresa;

    @NotNull(message = "es obligatorio")
    @DecimalMin(value = "0.01", message = "debe ser mayor que cero")
    @Digits(integer = 13, fraction = 2, message = "admite hasta 13 enteros y 2 decimales")
    private BigDecimal ingresoMensual;

    @NotNull(message = "es obligatorio")
    @Valid
    private GestoPagoDomicilioRequest domicilio;

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
