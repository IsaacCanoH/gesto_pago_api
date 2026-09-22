package com.proyecto.servicios.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class GestoPagoProductoCache {

    private static final String CLAVE = "gestopago:productos:todos";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public GestoPagoProductoCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${gestopago.product.cache-ttl}") Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException(
                    "gestopago.product.cache-ttl debe ser mayor que cero");
        }

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    public Optional<List<GestoPagoProductoResponse>> consultarTodos() {
        try {
            String json = redisTemplate.opsForValue().get(CLAVE);

            if (json == null) {
                return Optional.empty();
            }

            List<GestoPagoProductoResponse> productos = objectMapper.readValue(
                    json,
                    new TypeReference<List<GestoPagoProductoResponse>>() {
                    });

            return Optional.ofNullable(productos);
        } catch (DataAccessException | JsonProcessingException e) {
            log.warn(
                    "No fue posible leer el catálogo de Redis. Tipo: {}",
                    e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    public void guardarTodos(List<GestoPagoProductoResponse> productos) {
        try {
            String json = objectMapper.writeValueAsString(productos);
            redisTemplate.opsForValue().set(CLAVE, json, ttl);
        } catch (DataAccessException | JsonProcessingException e) {
            log.warn(
                    "No fue posible guardar el catálogo en Redis. Tipo: {}",
                    e.getClass().getSimpleName());
        }
    }

    public void invalidar() {
        try {
            redisTemplate.delete(CLAVE);
        } catch (DataAccessException e) {
            log.warn(
                    "No fue posible invalidar el catálogo de Redis. Tipo: {}",
                    e.getClass().getSimpleName());
        }
    }
}