package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.cache.GestoPagoProductoCache;
import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.mapper.GestoPagoProductoMapper;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.exception.GestoPagoExternalResponseException;
import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import com.proyecto.servicios.exception.GestoPagoCommunicationException;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;

import feign.Request;
import feign.RetryableException;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GestoPagoProductServiceImplTest {

    @Mock
    private GestoPagoProductClient gestoPagoProductClient;

    @Mock
    private GestoPagoTokenService gestoPagoTokenService;

    @Mock
    private GestoPagoProductoRepository productoRepository;

    @Mock
    private GestoPagoProductoMapper productoMapper;

    @Mock
    private GestoPagoProductoCache productoCache;

    @InjectMocks
    private GestoPagoProductServiceImpl gestoPagoProductService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                gestoPagoProductService, "idDistribuidor", 83);
        ReflectionTestUtils.setField(
                gestoPagoProductService, "codigoDispositivo", "GPS83-TPV-17");
    }

    @Test
    void debeSincronizarProductosCuandoLaRespuestaEsExitosa() {
        GestoPagoToken token = new GestoPagoToken();
        token.setToken("token-prueba");
        token.setActivo(true);

        String xml = """
                <RESPONSE>
                    <MENSAJE>
                        <CODIGO>01</CODIGO>
                        <TEXTO>Operacion realizada con exito</TEXTO>
                    </MENSAJE>
                    <PRODUCTOS>
                        <producto servicio="ABIB" producto="ABIB 100"
                            idServicio="2284" idProducto="14302"
                            idCatTipoServicio="13" tipoFront="1"
                            hasDigitoVerificador="false" precio="100.0"
                            showAyuda="false" tipoReferencia="a">
                            <legend>Descripción del producto</legend>
                        </producto>
                    </PRODUCTOS>
                </RESPONSE>
                """;

        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(token));
        when(gestoPagoProductClient.getProductList("Bearer token-prueba"))
                .thenReturn(xml);
        when(productoRepository.findAllByIdProductoIn(anySet()))
                .thenReturn(List.of());
        when(productoMapper.toEntity(any()))
                .thenReturn(new GestoPagoProducto());

        int resultado = gestoPagoProductService.sincronizarProductos();

        assertEquals(1, resultado);
        verify(gestoPagoProductClient).getProductList("Bearer token-prueba");
        verify(productoRepository).saveAll(any());
    }

    @Test
    void debeLanzarExcepcionCuandoGestoPagoRespondeCodigoNoExitoso() {
        GestoPagoToken token = new GestoPagoToken();
        token.setToken("token-prueba");
        token.setActivo(true);

        String xml = """
                <RESPONSE>
                    <MENSAJE>
                        <CODIGO>99</CODIGO>
                        <TEXTO>Error al consultar productos</TEXTO>
                    </MENSAJE>
                    <PRODUCTOS/>
                </RESPONSE>
                """;

        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(token));
        when(gestoPagoProductClient.getProductList("Bearer token-prueba"))
                .thenReturn(xml);

        assertThrows(
                GestoPagoExternalResponseException.class,
                () -> gestoPagoProductService.sincronizarProductos());

        verifyNoInteractions(productoRepository, productoMapper);
    }

    @Test
    void debeRenovarTokenYReintentarCuandoGestoPagoRechazaLaAutenticacion() {
        GestoPagoToken tokenAnterior = new GestoPagoToken();
        tokenAnterior.setToken("token-anterior");
        tokenAnterior.setActivo(true);

        GestoPagoToken tokenNuevo = new GestoPagoToken();
        tokenNuevo.setToken("token-nuevo");
        tokenNuevo.setActivo(true);

        String xmlExitoso = """
                <RESPONSE>
                    <MENSAJE>
                        <CODIGO>01</CODIGO>
                        <TEXTO>Operacion realizada con exito</TEXTO>
                    </MENSAJE>
                    <PRODUCTOS/>
                </RESPONSE>
                """;

        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(tokenAnterior));
        when(gestoPagoProductClient.getProductList("Bearer token-anterior"))
                .thenThrow(new GestoPagoAuthenticationException(
                        "GestoPago rechazó la autenticación"));
        when(gestoPagoTokenService.renovarTokenAhora())
                .thenReturn(tokenNuevo);
        when(gestoPagoProductClient.getProductList("Bearer token-nuevo"))
                .thenReturn(xmlExitoso);

        int productosRecibidos = gestoPagoProductService
                .descargarListaProductos()
                .getProductos()
                .size();

        assertEquals(0, productosRecibidos);
        verify(gestoPagoTokenService).renovarTokenAhora();
        verify(gestoPagoProductClient)
                .getProductList("Bearer token-anterior");
        verify(gestoPagoProductClient)
                .getProductList("Bearer token-nuevo");
    }

    @Test
    void debeLanzarExcepcionDeComunicacionCuandoExpiraLaConsulta() {
        GestoPagoToken token = new GestoPagoToken();
        token.setToken("token-prueba");
        token.setActivo(true);

        Request request = Request.create(
                Request.HttpMethod.GET,
                "https://gestopago.portalventas.net/sistema/service/getProductList.do",
                Collections.emptyMap(),
                Request.Body.empty(),
                null);

        RetryableException timeout = new RetryableException(
                0,
                "Tiempo de espera agotado",
                Request.HttpMethod.GET,
                new RuntimeException("timeout"),
                (Long) null,
                request);

        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(token));
        when(gestoPagoProductClient.getProductList("Bearer token-prueba"))
                .thenThrow(timeout);

        assertThrows(
                GestoPagoCommunicationException.class,
                () -> gestoPagoProductService.descargarListaProductos());

        verifyNoInteractions(productoRepository, productoMapper);
    }

    @Test
    void debeDevolverCatalogoDesdeRedisSinConsultarOtrasFuentes() {
        GestoPagoProductoResponse producto = new GestoPagoProductoResponse();
        producto.setIdProducto(14302L);
        producto.setProducto("ABIB 100");

        List<GestoPagoProductoResponse> catalogo = List.of(producto);

        when(productoCache.consultarTodos())
                .thenReturn(Optional.of(catalogo));

        List<GestoPagoProductoResponse> resultado = gestoPagoProductService.consultarProductos();

        assertEquals(catalogo, resultado);
        verify(productoCache).consultarTodos();
        verifyNoInteractions(
                productoRepository,
                productoMapper,
                gestoPagoProductClient,
                gestoPagoTokenService);
    }

    @Test
    void debeConsultarPostgresCuandoElCatalogoNoEstaEnRedis() {
        GestoPagoProducto entidad = new GestoPagoProducto();
        entidad.setIdProducto(14302L);
        entidad.setProducto("ABIB 100");

        GestoPagoProductoResponse producto = new GestoPagoProductoResponse();
        producto.setIdProducto(14302L);
        producto.setProducto("ABIB 100");

        List<GestoPagoProducto> entidades = List.of(entidad);
        List<GestoPagoProductoResponse> catalogo = List.of(producto);

        when(productoCache.consultarTodos())
                .thenReturn(Optional.empty());
        when(productoRepository.findAll())
                .thenReturn(entidades);
        when(productoMapper.toResponseList(entidades))
                .thenReturn(catalogo);

        List<GestoPagoProductoResponse> resultado = gestoPagoProductService.consultarProductos();

        assertEquals(catalogo, resultado);
        verify(productoCache).consultarTodos();
        verify(productoRepository).findAll();
        verify(productoMapper).toResponseList(entidades);
        verifyNoInteractions(gestoPagoProductClient, gestoPagoTokenService);
        verify(productoCache).guardarTodos(catalogo);
    }

    @Test
    void debeConsultarApiYGuardarCuandoRedisYPostgresNoTienenCatalogo() {
        GestoPagoToken token = new GestoPagoToken();
        token.setToken("token-prueba");
        token.setActivo(true);

        String xml = """
                <RESPONSE>
                    <MENSAJE>
                        <CODIGO>01</CODIGO>
                        <TEXTO>Operacion realizada con exito</TEXTO>
                    </MENSAJE>
                    <PRODUCTOS>
                        <producto idProducto="14302"
                                  servicio="ABIB"
                                  producto="ABIB 100"/>
                    </PRODUCTOS>
                </RESPONSE>
                """;

        GestoPagoProducto entidad = new GestoPagoProducto();
        entidad.setIdProducto(14302L);
        entidad.setServicio("ABIB");
        entidad.setProducto("ABIB 100");

        GestoPagoProductoResponse producto = new GestoPagoProductoResponse();
        producto.setIdProducto(14302L);
        producto.setServicio("ABIB");
        producto.setProducto("ABIB 100");

        List<GestoPagoProducto> entidades = List.of(entidad);
        List<GestoPagoProductoResponse> catalogo = List.of(producto);

        when(productoCache.consultarTodos())
                .thenReturn(Optional.empty());
        when(productoRepository.findAll())
                .thenReturn(List.of())
                .thenReturn(entidades);
        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(token));
        when(gestoPagoProductClient.getProductList("Bearer token-prueba"))
                .thenReturn(xml);
        when(productoRepository.findAllByIdProductoIn(anySet()))
                .thenReturn(List.of());
        when(productoMapper.toEntity(any()))
                .thenReturn(entidad);
        when(productoMapper.toResponseList(entidades))
                .thenReturn(catalogo);

        List<GestoPagoProductoResponse> resultado = gestoPagoProductService.consultarProductos();

        assertEquals(catalogo, resultado);

        InOrder orden = inOrder(
                productoCache,
                productoRepository,
                gestoPagoProductClient);

        orden.verify(productoCache).consultarTodos();
        orden.verify(productoRepository).findAll();
        orden.verify(gestoPagoProductClient)
                .getProductList("Bearer token-prueba");
        orden.verify(productoRepository).saveAll(entidades);
        orden.verify(productoCache).invalidar();
        orden.verify(productoRepository).findAll();
        orden.verify(productoCache).guardarTodos(catalogo);
    }
}