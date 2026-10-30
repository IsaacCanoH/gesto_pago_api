package com.proyecto.servicios.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.proyecto.servicios.cache.GestoPagoProductoCache;
import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import com.proyecto.servicios.exception.GestoPagoCommunicationException;
import com.proyecto.servicios.exception.GestoPagoExternalResponseException;
import com.proyecto.servicios.mapper.GestoPagoProductoMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoPaginaRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;

import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private GestoPagoCatalogoPersistenceService catalogoPersistenceService;

    @InjectMocks
    private GestoPagoProductServiceImpl gestoPagoProductService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gestoPagoProductService, "idDistribuidor", 83);
        ReflectionTestUtils.setField(gestoPagoProductService,
                "codigoDispositivo", "GPS83-TPV-17");
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
        when(catalogoPersistenceService.guardarCatalogo(any())).thenReturn(1);

        int resultado = gestoPagoProductService.sincronizarProductos();

        assertEquals(1, resultado);
        verify(gestoPagoProductClient).getProductList("Bearer token-prueba");
        verify(catalogoPersistenceService).guardarCatalogo(any());
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

        assertThrows(GestoPagoExternalResponseException.class,
                () -> gestoPagoProductService.sincronizarProductos());
        verifyNoInteractions(productoRepository, productoMapper,
                catalogoPersistenceService);
    }

    @Test
    void catalogoVacioNoDesactivaProductosExistentes() {
        GestoPagoToken token = new GestoPagoToken();
        token.setToken("token-prueba");
        token.setActivo(true);

        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(token));
        when(gestoPagoProductClient.getProductList("Bearer token-prueba"))
                .thenReturn(
                        "<RESPONSE><MENSAJE><CODIGO>01</CODIGO></MENSAJE>"
                                + "<PRODUCTOS/></RESPONSE>");

        assertThrows(GestoPagoExternalResponseException.class,
                () -> gestoPagoProductService.sincronizarProductos());
        verifyNoInteractions(catalogoPersistenceService, productoCache);
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
        when(gestoPagoTokenService.renovarTokenAhora()).thenReturn(tokenNuevo);
        when(gestoPagoProductClient.getProductList("Bearer token-nuevo"))
                .thenReturn(xmlExitoso);

        int productosRecibidos = gestoPagoProductService
                .descargarListaProductos()
                .getProductos()
                .size();

        assertEquals(0, productosRecibidos);
        verify(gestoPagoTokenService).renovarTokenAhora();
        verify(gestoPagoProductClient).getProductList("Bearer token-anterior");
        verify(gestoPagoProductClient).getProductList("Bearer token-nuevo");
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

        assertThrows(GestoPagoCommunicationException.class,
                () -> gestoPagoProductService.descargarListaProductos());
        verifyNoInteractions(productoRepository, productoMapper,
                catalogoPersistenceService);
    }

    @Test
    void debeDevolverCatalogoDesdeRedisSinConsultarOtrasFuentes() {
        GestoPagoProductoResponse producto = new GestoPagoProductoResponse();
        producto.setIdProducto(14302L);
        producto.setProducto("ABIB 100");

        List<GestoPagoProductoResponse> catalogo = List.of(producto);
        when(productoCache.consultarTodos()).thenReturn(Optional.of(catalogo));

        List<GestoPagoProductoResponse> resultado =
                gestoPagoProductService.consultarProductos();

        assertEquals(catalogo, resultado);
        verify(productoCache).consultarTodos();
        verifyNoInteractions(productoRepository, productoMapper,
                gestoPagoProductClient, gestoPagoTokenService);
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

        when(productoCache.consultarTodos()).thenReturn(Optional.empty());
        when(productoRepository.findAllByActivoTrue()).thenReturn(entidades);
        when(productoMapper.toResponseList(entidades)).thenReturn(catalogo);

        List<GestoPagoProductoResponse> resultado =
                gestoPagoProductService.consultarProductos();

        assertEquals(catalogo, resultado);
        verify(productoCache).consultarTodos();
        verify(productoRepository).findAllByActivoTrue();
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

        when(productoCache.consultarTodos()).thenReturn(Optional.empty());
        when(productoRepository.findAllByActivoTrue())
                .thenReturn(List.of())
                .thenReturn(entidades);
        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(token));
        when(gestoPagoProductClient.getProductList("Bearer token-prueba"))
                .thenReturn(xml);
        when(catalogoPersistenceService.guardarCatalogo(any())).thenReturn(1);
        when(productoMapper.toResponseList(entidades)).thenReturn(catalogo);

        List<GestoPagoProductoResponse> resultado =
                gestoPagoProductService.consultarProductos();

        assertEquals(catalogo, resultado);

        InOrder orden = inOrder(productoCache, productoRepository,
                catalogoPersistenceService, gestoPagoProductClient);
        orden.verify(productoCache).consultarTodos();
        orden.verify(productoRepository).findAllByActivoTrue();
        orden.verify(gestoPagoProductClient)
                .getProductList("Bearer token-prueba");
        orden.verify(catalogoPersistenceService).guardarCatalogo(any());
        orden.verify(productoCache).invalidar();
        orden.verify(productoRepository).findAllByActivoTrue();
        orden.verify(productoCache).guardarTodos(catalogo);
    }

    @Test
    void paginaProductosDesdePostgresSinCargarTodoElCatalogo() {
        GestoPagoProducto entidad = new GestoPagoProducto();
        entidad.setIdProducto(14302L);
        GestoPagoProductoResponse respuesta = new GestoPagoProductoResponse();
        respuesta.setIdProducto(14302L);
        GestoPagoProductoPaginaRequest request = new GestoPagoProductoPaginaRequest();
        request.setPagina(1);
        request.setTamanio(2);

        when(productoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entidad), PageRequest.of(1, 2), 3));
        when(productoMapper.toResponse(entidad)).thenReturn(respuesta);

        var pagina = gestoPagoProductService.consultarProductosPaginados(request);

        assertEquals(3, pagina.getTotalElements());
        assertEquals(14302L, pagina.getContent().get(0).getIdProducto());

        ArgumentCaptor<Pageable> paginacion =
                ArgumentCaptor.forClass(Pageable.class);
        verify(productoRepository).findAll(paginacion.capture());
        assertEquals(1, paginacion.getValue().getPageNumber());
        assertEquals(2, paginacion.getValue().getPageSize());
        assertEquals("idProducto",
                paginacion.getValue().getSort().iterator().next().getProperty());
        verifyNoInteractions(productoCache, gestoPagoProductClient,
                gestoPagoTokenService);
    }

    @Test
    void paginaVaciaFueraDelRangoNoSincronizaSiExistenProductos() {
        GestoPagoProductoPaginaRequest request =
                new GestoPagoProductoPaginaRequest();
        request.setPagina(2);
        request.setTamanio(2);

        when(productoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 2), 3));

        var pagina = gestoPagoProductService.consultarProductosPaginados(request);

        assertEquals(0, pagina.getNumberOfElements());
        verify(productoRepository, times(1)).findAll(any(Pageable.class));
        verifyNoInteractions(gestoPagoProductClient, gestoPagoTokenService);
    }

    @Test
    void sincronizaSoloSiElCatalogoPaginadoEstaVacio() {
        GestoPagoProducto entidad = new GestoPagoProducto();
        entidad.setIdProducto(14302L);

        GestoPagoProductoPaginaRequest request =
                new GestoPagoProductoPaginaRequest();

        when(productoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()))
                .thenReturn(new PageImpl<>(List.of(entidad)));

        var servicio = spy(gestoPagoProductService);
        doReturn(1).when(servicio).sincronizarProductos();

        var pagina = servicio.consultarProductosPaginados(request);

        assertEquals(1, pagina.getTotalElements());
        verify(servicio).sincronizarProductos();
        verify(productoRepository, times(2)).findAll(any(Pageable.class));
    }
}