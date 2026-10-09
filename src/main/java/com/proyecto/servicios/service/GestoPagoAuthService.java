package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoLoginRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoLoginResponse;
import com.proyecto.servicios.security.GestoPagoPrincipal;

public interface GestoPagoAuthService {
    GestoPagoLoginResponse iniciarSesion(GestoPagoLoginRequest request);

    GestoPagoPrincipal obtenerPrincipalActivo(Integer usuarioId);
}
