package com.proyecto.servicios.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.proyecto.servicios.exception.GestoPagoCredencialesInvalidasException;
import com.proyecto.servicios.exception.GestoPagoUsuarioInactivoException;
import com.proyecto.servicios.service.GestoPagoAuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GestoPagoJwtFilter extends OncePerRequestFilter {
    private static final String BEARER = "Bearer ";

    private final GestoPagoJwtService jwtService;
    private final GestoPagoAuthService authService;
    private final GestoPagoSecurityErrorWriter errorWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return HttpMethod.OPTIONS.matches(request.getMethod())
                || (HttpMethod.POST.matches(request.getMethod())
                        && ("/auth/login".equals(path) || "/clientes".equals(path)))
                || path.startsWith("/v3/api-docs/") || "/v3/api-docs".equals(path)
                || path.startsWith("/swagger-ui/") || "/swagger-ui.html".equals(path)
                || "/actuator/health".equals(path) || "/error".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null) {
            if (!authorization.startsWith(BEARER)
                    || authorization.substring(BEARER.length()).isBlank()) {
                errorWriter.escribir(response, HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
                return;
            }

            try {
                Integer usuarioId = jwtService.verificar(authorization.substring(BEARER.length()));
                GestoPagoPrincipal principal = authService.obtenerPrincipalActivo(usuarioId);
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (GestoPagoCredencialesInvalidasException exception) {
                SecurityContextHolder.clearContext();
                errorWriter.escribir(response, HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
                return;
            } catch (GestoPagoUsuarioInactivoException exception) {
                SecurityContextHolder.clearContext();
                errorWriter.escribir(response, HttpStatus.FORBIDDEN, exception.getMessage());
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
