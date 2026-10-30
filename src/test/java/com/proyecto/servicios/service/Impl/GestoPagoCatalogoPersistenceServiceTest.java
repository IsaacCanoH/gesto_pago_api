package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.exception.GestoPagoExternalResponseException;
import com.proyecto.servicios.mapper.GestoPagoProductoMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse.Producto;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;

@ExtendWith(MockitoExtension.class)
class GestoPagoCatalogoPersistenceServiceTest {
    @Mock private GestoPagoProductoRepository productoRepository;
    @Mock private GestoPagoProductoMapper mapper;

    @Test
    void noDesactivaTodoSiRecibeUnaListaVacia() {
        assertThrows(GestoPagoExternalResponseException.class,
                () -> new GestoPagoCatalogoPersistenceService(productoRepository, mapper)
                        .guardarCatalogo(Map.of()));
        verifyNoInteractions(productoRepository, mapper);
    }

    @Test
    void desactivaLosProductosAusentesSinBorrarlosYReactivaLosQueRegresan() {
        GestoPagoProducto vigente = new GestoPagoProducto();
        vigente.setIdProducto(1L);
        vigente.setActivo(false);
        GestoPagoProducto retirado = new GestoPagoProducto();
        retirado.setIdProducto(2L);
        retirado.setActivo(true);
        Producto recibido = new Producto();
        recibido.setIdProducto(1L);

        when(productoRepository.findAllByIdProductoIn(any())).thenReturn(List.of(vigente));
        when(productoRepository.findAllByActivoTrue()).thenReturn(List.of(retirado));

        int total = new GestoPagoCatalogoPersistenceService(productoRepository, mapper)
                .guardarCatalogo(Map.of(1L, recibido));

        assertEquals(1, total);
        assertTrue(vigente.getActivo());
        assertFalse(retirado.getActivo());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<GestoPagoProducto>> cambios = ArgumentCaptor.forClass(Iterable.class);
        verify(productoRepository).saveAll(cambios.capture());
        assertEquals(List.of(vigente, retirado), cambios.getValue());
    }
}
