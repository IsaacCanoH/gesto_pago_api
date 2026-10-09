package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GestoPagoDomicilioParcialRequest implements TextoRecortable {

    @Size(max = 150, message = "no debe superar 150 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String calle;

    @Size(max = 20, message = "no debe superar 20 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String numeroExterior;

    @Size(max = 20, message = "no debe superar 20 caracteres")
    private String numeroInterior;

    @Size(max = 100, message = "no debe superar 100 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String colonia;

    @Size(max = 100, message = "no debe superar 100 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String municipio;

    @Size(max = 100, message = "no debe superar 100 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
    private String estado;

    @Pattern(regexp = "^[0-9]{5}$", message = "debe tener exactamente 5 dígitos")
    private String codigoPostal;

    @Size(max = 60, message = "no debe superar 60 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "no puede contener solo espacios")
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
