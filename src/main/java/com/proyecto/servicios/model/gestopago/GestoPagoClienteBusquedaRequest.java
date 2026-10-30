package com.proyecto.servicios.model.gestopago;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GestoPagoClienteBusquedaRequest implements TextoRecortable {
    @Positive(message = "debe ser mayor que cero")
    private Integer id;

    @Size(max = 18, message = "no debe superar 18 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "solo admite letras y números")
    private String curp;

    @Size(max = 13, message = "no debe superar 13 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9&Ññ]+$", message = "contiene caracteres no válidos")
    private String rfc;

    @Email(message = "debe tener un formato válido")
    @Size(min = 1, max = 100, message = "debe tener entre 1 y 100 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío")
    private String correo;

    @Pattern(regexp = "^[0-9]{20}$", message = "debe tener exactamente 20 dígitos")
    private String numeroCuenta;

    private Boolean activa;
    private LocalDate desde;
    private LocalDate hasta;
    private boolean parcial;
    private boolean detalle;

    @Min(value = 0, message = "debe ser mayor o igual a cero")
    private int pagina = 0;

    @Min(value = 1, message = "debe ser mayor o igual a uno")
    @Max(value = 100, message = "no debe superar 100")
    private int tamanio = 20;

    @Override
    public void recortarEspacios() {
        curp = TextoRecortable.trim(curp);
        rfc = TextoRecortable.trim(rfc);
        correo = TextoRecortable.trim(correo);
        numeroCuenta = TextoRecortable.trim(numeroCuenta);
    }
}
