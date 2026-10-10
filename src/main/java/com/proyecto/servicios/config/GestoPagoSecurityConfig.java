package com.proyecto.servicios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.proyecto.servicios.security.GestoPagoJwtFilter;
import com.proyecto.servicios.security.GestoPagoJwtService;
import com.proyecto.servicios.security.GestoPagoSecurityErrorWriter;
import com.proyecto.servicios.service.GestoPagoAuthService;

@Configuration
public class GestoPagoSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            GestoPagoJwtService jwtService,
            GestoPagoAuthService authService,
            GestoPagoSecurityErrorWriter errorWriter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                errorWriter.escribir(response, HttpStatus.UNAUTHORIZED,
                                        "Se requiere un token de acceso válido"))
                        .accessDeniedHandler((request, response, exception) ->
                                errorWriter.escribir(response, HttpStatus.FORBIDDEN,
                                        "Acceso denegado")))
                .addFilterBefore(
                        new GestoPagoJwtFilter(jwtService, authService, errorWriter),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
