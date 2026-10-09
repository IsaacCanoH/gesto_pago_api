package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoCambioContrasenaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoUsuarioConsultaResponse;

public interface GestoPagoUsuarioService {
    GestoPagoUsuarioConsultaResponse consultarUsuario(Integer id, Integer solicitanteId);

    void cambiarContrasena(Integer id, Integer solicitanteId, GestoPagoCambioContrasenaRequest request);
}
