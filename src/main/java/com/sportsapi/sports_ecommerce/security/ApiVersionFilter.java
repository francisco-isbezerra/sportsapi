package com.sportsapi.sports_ecommerce.security;

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
import java.util.List;

@Component
@org.springframework.core.annotation.Order(0) // Executa antes de todos os filtros de segurança e negócio
public class ApiVersionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String versionHeader = request.getHeader("X-API-Version");

            // Se o header de versão for nulo ou vazio, envelopamos a requisição para assumir "1" como padrão
            if (versionHeader == null || versionHeader.trim().isEmpty()) {
                HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getHeader(String name) {
                        if ("X-API-Version".equalsIgnoreCase(name)) {
                            return "1";
                        }
                        return super.getHeader(name);
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        if ("X-API-Version".equalsIgnoreCase(name)) {
                            return Collections.enumeration(Collections.singletonList("1"));
                        }
                        return super.getHeaders(name);
                    }

                    @Override
                    public Enumeration<String> getHeaderNames() {
                        List<String> names = Collections.list(super.getHeaderNames());
                        if (!names.contains("X-API-Version") && !names.contains("x-api-version")) {
                            names.add("X-API-Version");
                        }
                        return Collections.enumeration(names);
                    }
                };

                filterChain.doFilter(wrappedRequest, response);
            } else {
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            System.err.println(">>> [WARN] Erro no ApiVersionFilter, prosseguindo com requisição original: " + e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}
