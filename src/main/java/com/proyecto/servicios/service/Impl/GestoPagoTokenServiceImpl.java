package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoAuthClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.mapper.GestoPagoTokenMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoTokenRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import com.proyecto.servicios.exception.GestoPagoFeignExceptionTranslator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestoPagoTokenServiceImpl implements GestoPagoTokenService {

    private final GestoPagoAuthClient gestoPagoAuthClient;
    private final GestoPagoTokenRepository tokenRepository;
    private final GestoPagoTokenMapper tokenMapper;

    @Value("${gestopago.auth.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo}")
    private String codigoDispositivo;

    @Value("${gestopago.auth.password}")
    private String password;

    @Override
    @Scheduled(cron = "${gestopago.auth.refresh-cron:0 5 0 * * *}",
            zone = "${gestopago.product.time-zone:America/Mexico_City}")
    public void renovarToken() {
        try {
            renovarTokenAhora();
        } catch (Exception e) {
            log.error(
                    "Error al renovar token de GestoPago. Tipo: {}",
                    e.getClass().getSimpleName());
        }
    }

    @Override
    public GestoPagoToken renovarTokenAhora() {
        log.info("Renovando token GestoPago para distribuidor={}",
                idDistribuidor);

        GestoPagoAuthResponse response;

        try {
            response = gestoPagoAuthClient.authenticate(
                    idDistribuidor,
                    codigoDispositivo,
                    password);
        } catch (FeignException e) {
            throw GestoPagoFeignExceptionTranslator.traducir(e);
        }

        if (response == null || response.getToken() == null) {
            throw new GestoPagoAuthenticationException(
                    "GestoPago no devolvió un token válido");
        }

        GestoPagoToken tokenEntity = tokenRepository
                .findByIdDistribuidorAndCodigoDispositivo(
                        idDistribuidor,
                        codigoDispositivo)
                .map(existing -> {
                    tokenMapper.updateEntity(response, existing);
                    return existing;
                })
                .orElseGet(() -> {
                    GestoPagoToken nuevo = tokenMapper.toEntity(response);
                    nuevo.setIdDistribuidor(idDistribuidor);
                    nuevo.setCodigoDispositivo(codigoDispositivo);
                    nuevo.setActivo(true);
                    return nuevo;
                });

        GestoPagoToken tokenGuardado = tokenRepository.save(tokenEntity);

        log.info("Token GestoPago renovado correctamente");

        return tokenGuardado;
    }

    @Override
    public Optional<GestoPagoToken> obtenerTokenActivo(Integer idDistribuidor, String codigoDispositivo) {
        return tokenRepository.findByIdDistribuidorAndCodigoDispositivo(idDistribuidor, codigoDispositivo);
    }
}
