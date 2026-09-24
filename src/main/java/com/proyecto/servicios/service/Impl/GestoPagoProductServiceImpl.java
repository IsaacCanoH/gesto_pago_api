package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.service.GestoPagoProductService;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse.Producto;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.mapper.GestoPagoProductoMapper;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.exception.GestoPagoExternalResponseException;
import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import com.proyecto.servicios.exception.GestoPagoFeignExceptionTranslator;
import com.proyecto.servicios.exception.GestoPagoSynchronizationInProgressException;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import com.proyecto.servicios.cache.GestoPagoProductoCache;

import java.util.concurrent.atomic.AtomicBoolean;
import feign.FeignException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GestoPagoProductServiceImpl implements GestoPagoProductService {

        private final GestoPagoProductClient gestoPagoProductClient;
        private final GestoPagoTokenService gestoPagoTokenService;
        private final GestoPagoProductoRepository productoRepository;
        private final GestoPagoProductoMapper productoMapper;
        private final AtomicBoolean sincronizacionEnProceso = new AtomicBoolean(false);
        private final GestoPagoProductoCache productoCache;

        @Value("${gestopago.auth.id-distribuidor}")
        private Integer idDistribuidor;

        @Value("${gestopago.auth.codigo-dispositivo}")
        private String codigoDispositivo;

        @Override
        public GestoPagoProductListResponse descargarListaProductos() {
                GestoPagoToken token = obtenerToken();

                log.info("Consultando lista de productos de GestoPago");

                String xml = consultarConRenovacionDeToken(token);
                GestoPagoProductListResponse response = parsearRespuesta(xml);
                log.info("Lista de productos recibida: {} registros", response.getProductos().size());
                return response;
        }

        private String consultarConRenovacionDeToken(GestoPagoToken token) {
                try {
                        return consultarProductos(token);
                } catch (GestoPagoAuthenticationException e) {
                        log.warn("GestoPago rechazó el token; se renovará una vez");
                        return consultarProductos(gestoPagoTokenService.renovarTokenAhora());
                }
        }

        private GestoPagoProductListResponse parsearRespuesta(String xml) {
                try {
                        JAXBContext context = JAXBContext.newInstance(GestoPagoProductListResponse.class);
                        Unmarshaller unmarshaller = context.createUnmarshaller();

                        GestoPagoProductListResponse response = (GestoPagoProductListResponse) unmarshaller.unmarshal(
                                        new StringReader(xml));

                        if (response.getMensaje() == null
                                        || !"01".equals(response.getMensaje().getCodigo())) {
                                throw new GestoPagoExternalResponseException(
                                                "GestoPago no confirmó una consulta exitosa de productos");
                        }

                        return response;
                } catch (JAXBException e) {
                        throw new GestoPagoExternalResponseException(
                                        "No fue posible interpretar el XML de productos de GestoPago", e);
                }
        }

        private GestoPagoToken obtenerToken() {
                return gestoPagoTokenService
                                .obtenerTokenActivo(idDistribuidor, codigoDispositivo)
                                .filter(token -> Boolean.TRUE.equals(token.getActivo()))
                                .orElseGet(() -> {
                                        log.info("No existe token activo; se generará uno nuevo");
                                        return gestoPagoTokenService.renovarTokenAhora();
                                });
        }

        private String consultarProductos(GestoPagoToken token) {
                try {
                        return gestoPagoProductClient.getProductList(
                                        "Bearer " + token.getToken());
                } catch (FeignException e) {
                        throw GestoPagoFeignExceptionTranslator.traducir(e);
                }
        }

        @Override
        public int sincronizarProductos() {
                if (!sincronizacionEnProceso.compareAndSet(false, true)) {
                        throw new GestoPagoSynchronizationInProgressException(
                                        "Ya existe una sincronización de productos en curso");
                }

                try {
                        Map<Long, Producto> productosPorId = indexarProductos(descargarListaProductos());

                        if (productosPorId.isEmpty()) {
                                log.warn("GestoPago no devolvió productos válidos para sincronizar");
                                return 0;
                        }

                        List<GestoPagoProducto> productosParaGuardar = prepararProductos(productosPorId);
                        productoRepository.saveAll(productosParaGuardar);
                        productoCache.invalidar();
                        log.info("Productos de GestoPago sincronizados: {}", productosParaGuardar.size());
                        return productosParaGuardar.size();
                } finally {
                        sincronizacionEnProceso.set(false);
                }
        }

        private Map<Long, Producto> indexarProductos(GestoPagoProductListResponse response) {
                Map<Long, Producto> productosPorId = new LinkedHashMap<>();
                for (Producto producto : response.getProductos()) {
                        if (producto.getIdProducto() != null) {
                                productosPorId.put(producto.getIdProducto(), producto);
                        }
                }
                return productosPorId;
        }

        private List<GestoPagoProducto> prepararProductos(Map<Long, Producto> productosPorId) {
                Map<Long, GestoPagoProducto> existentes = productoRepository
                                .findAllByIdProductoIn(productosPorId.keySet())
                                .stream()
                                .collect(Collectors.toMap(GestoPagoProducto::getIdProducto, Function.identity()));

                List<GestoPagoProducto> productosParaGuardar = new ArrayList<>();
                for (Producto productoXml : productosPorId.values()) {
                        GestoPagoProducto producto = existentes.get(productoXml.getIdProducto());
                        if (producto == null) {
                                producto = productoMapper.toEntity(productoXml);
                        } else {
                                productoMapper.updateEntity(productoXml, producto);
                        }
                        producto.setActivo(true);
                        productosParaGuardar.add(producto);
                }
                return productosParaGuardar;
        }

        @Override
        public List<GestoPagoProductoResponse> consultarProductos() {
                return productoCache.consultarTodos()
                                .orElseGet(this::consultarYCachearProductos);
        }

        private List<GestoPagoProductoResponse> consultarYCachearProductos() {
                List<GestoPagoProducto> productos = productoRepository.findAll();
                if (productos.isEmpty()) {
                        sincronizarProductos();
                        productos = productoRepository.findAll();
                }
                List<GestoPagoProductoResponse> catalogo = productoMapper.toResponseList(productos);
                if (!catalogo.isEmpty()) {
                        productoCache.guardarTodos(catalogo);
                }
                return catalogo;
        }
}
