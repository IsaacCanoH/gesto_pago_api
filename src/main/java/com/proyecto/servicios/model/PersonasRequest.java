package com.proyecto.servicios.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonasRequest {

    @NotBlank(message = "es obligatorio")
    @Size(max = 50, message = "no debe superar 50 caracteres")
    private String nombre;
    @NotBlank(message = "es obligatorio")
    @Size(max = 50, message = "no debe superar 50 caracteres")
    private String apellidoP;
    @NotBlank(message = "es obligatorio")
    @Size(max = 50, message = "no debe superar 50 caracteres")
    private String apellidoMaterno;

}
