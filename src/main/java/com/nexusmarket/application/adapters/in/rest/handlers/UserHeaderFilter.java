package com.nexusmarket.application.adapters.in.rest.handlers;

import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.infrastructure.security.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Permite enviar en X-User-Id el documento del usuario (cedula del comprador,
 * NIT del vendedor) igual que en el ingreso web: si el valor coincide con un
 * documento registrado se reemplaza por el id de ese usuario. Si no coincide,
 * el valor sigue igual y se trata como id.
 */
@Component
public class UserHeaderFilter extends OncePerRequestFilter {

    private final UserRepositoryPort userRepositoryPort;

    public UserHeaderFilter(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(request.getContextPath() + "/api");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(CurrentUser.HEADER);
        if (header == null || header.isBlank()) {
            chain.doFilter(request, response);
            return;
        }
        String resolved = userRepositoryPort.findByIdentityDocument(header.trim())
                .map(User::getId)
                .map(Object::toString)
                .orElse(header);
        chain.doFilter(new HeaderOverride(request, resolved), response);
    }

    /** Peticion con el valor de X-User-Id ya traducido a id. */
    private static final class HeaderOverride extends HttpServletRequestWrapper {

        private final String userId;

        HeaderOverride(HttpServletRequest request, String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public String getHeader(String name) {
            return CurrentUser.HEADER.equalsIgnoreCase(name) ? userId : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return CurrentUser.HEADER.equalsIgnoreCase(name)
                    ? Collections.enumeration(Collections.singletonList(userId)) : super.getHeaders(name);
        }
    }
}
