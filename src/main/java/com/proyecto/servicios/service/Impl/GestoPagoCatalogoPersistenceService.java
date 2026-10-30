package com.proyecto.servicios.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.exception.GestoPagoExternalResponseException;
import com.proyecto.servicios.mapper.GestoPagoProductoMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse.Producto;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestoPagoCatalogoPersistenceService {
    private final GestoPagoProductoRepository productoRepository;
    private final GestoPagoProductoMapper productoMapper;

    @Transactional(transactionManager = "sfTransactionManager")
    public int guardarCatalogo(Map<Long, Producto> productosPorId) {
        if (productosPorId == null || productosPorId.isEmpty()) {
            throw new GestoPagoExternalResponseException("No se puede sincronizar un catálogo vacío");
        }
        Map<Long, GestoPagoProducto> existentes = productoRepository
                .findAllByIdProductoIn(productosPorId.keySet()).stream()
                .collect(Collectors.toMap(GestoPagoProducto::getIdProducto, Function.identity()));

        List<GestoPagoProducto> cambios = new ArrayList<>();
        for (Producto recibido : productosPorId.values()) {
            GestoPagoProducto producto = existentes.get(recibido.getIdProducto());
            if (producto == null) {
                producto = productoMapper.toEntity(recibido);
            } else {
                productoMapper.updateEntity(recibido, producto);
            }
            producto.setActivo(true);
            cambios.add(producto);
        }

        for (GestoPagoProducto vigente : productoRepository.findAllByActivoTrue()) {
            if (!productosPorId.containsKey(vigente.getIdProducto())) {
                vigente.setActivo(false);
                cambios.add(vigente);
            }
        }
        productoRepository.saveAll(cambios);
        return productosPorId.size();
    }
}
