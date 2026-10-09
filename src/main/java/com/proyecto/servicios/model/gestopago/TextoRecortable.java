package com.proyecto.servicios.model.gestopago;

public interface TextoRecortable {
    void recortarEspacios();

    static String trim(String valor) {
        return valor == null ? null : valor.trim();
    }

    static String trimOpcional(String valor) {
        String recortado = trim(valor);
        return recortado == null || recortado.isEmpty() ? null : recortado;
    }
}
