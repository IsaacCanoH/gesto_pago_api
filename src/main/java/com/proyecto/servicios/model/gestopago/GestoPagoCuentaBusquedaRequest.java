package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GestoPagoCuentaBusquedaRequest implements TextoRecortable {
    @Pattern(regexp = "^[0-9]{20}$", message = "debe tener exactamente 20 dígitos")
    private String numeroCuenta;
    private Boolean soloActivas;

    @Min(value = 0, message = "debe ser mayor o igual a cero")
    private int pagina = 0;

    @Min(value = 1, message = "debe ser mayor o igual a uno")
    @Max(value = 100, message = "no debe superar 100")
    private int tamanio = 20;

    @Override
    public void recortarEspacios() {
        numeroCuenta = TextoRecortable.trim(numeroCuenta);
    }
}
