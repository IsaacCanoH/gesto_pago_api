package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GestoPagoDomicilioRequest implements TextoRecortable {
    @NotBlank(message = "es obligatoria")
    @Size(max = 150, message = "no debe superar 150 caracteres")
    private String calle;

    @NotBlank(message = "es obligatorio")
    @Size(max = 20, message = "no debe superar 20 caracteres")
    private String numeroExterior;

    @Size(max = 20, message = "no debe superar 20 caracteres")
    private String numeroInterior;

    @NotBlank(message = "es obligatoria")
    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String colonia;

    @NotBlank(message = "es obligatorio")
    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String municipio;

    @NotBlank(message = "es obligatorio")
    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String estado;

    @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "^[0-9]{5}$", message = "debe tener exactamente 5 dígitos")
    private String codigoPostal;

    @NotBlank(message = "es obligatorio")
    @Size(max = 60, message = "no debe superar 60 caracteres")
    private String pais;

    @Override
    public void recortarEspacios() {
        calle = TextoRecortable.trim(calle);
        numeroExterior = TextoRecortable.trim(numeroExterior);
        numeroInterior = TextoRecortable.trimOpcional(numeroInterior);
        colonia = TextoRecortable.trim(colonia);
        municipio = TextoRecortable.trim(municipio);
        estado = TextoRecortable.trim(estado);
        codigoPostal = TextoRecortable.trim(codigoPostal);
        pais = TextoRecortable.trim(pais);
    }
}
