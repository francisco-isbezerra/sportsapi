package com.sportsapi.sports_ecommerce.security;

import com.sportsapi.sports_ecommerce.model.IdempotencyRecord;
import com.sportsapi.sports_ecommerce.repository.IdempotencyRecordRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Optional;

@Component
@org.springframework.core.annotation.Order(3)
public class IdempotencyFilter extends OncePerRequestFilter {

    @Autowired
    private IdempotencyRecordRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            String method = request.getMethod();

            // 1. Aplicar apenas em operações POST de negócio
            if (!method.equalsIgnoreCase("POST") || 
                path.startsWith("/swagger-ui") || 
                path.startsWith("/v3/api-docs") || 
                path.startsWith("/api-docs") || 
                path.startsWith("/webjars") || 
                path.startsWith("/h2-console") || 
                path.equals("/api/auth/keys/gerar")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extrair o cabeçalho de idempotência
            String key = request.getHeader("X-Idempotency-Key");

            if (key == null || key.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"timestamp\":\"" + java.time.LocalDateTime.now() + "\"," +
                        "\"status\":400," +
                        "\"error\":\"Idempotency Error\"," +
                        "\"message\":\"O cabeçalho X-Idempotency-Key é obrigatório para operações POST.\"}"
                );
                return;
            }

            // Lookup dinâmico e seguro do IdempotencyRecordRepository caso a injeção via Autowired atrase
            if (repository == null) {
                try {
                    org.springframework.web.context.WebApplicationContext wac = 
                            org.springframework.web.context.support.WebApplicationContextUtils
                            .getWebApplicationContext(request.getServletContext());
                    if (wac != null) {
                        repository = wac.getBean(IdempotencyRecordRepository.class);
                    }
                } catch (Exception ex) {
                    System.err.println(">>> [WARN] Não foi possível obter o IdempotencyRecordRepository dinamicamente: " + ex.getMessage());
                }
            }

            if (repository == null) {
                // Caso extremo, prossegue sem idempotência para não travar a API
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Verificar se a chave já foi processada
            Optional<IdempotencyRecord> recordOpt = repository.findById(key);
            if (recordOpt.isPresent()) {
                IdempotencyRecord record = recordOpt.get();
                response.setStatus(record.getResponseStatus());
                response.setContentType("application/json;charset=UTF-8");
                // Adiciona um header informativo indicando que o resultado veio do cache de idempotência
                response.setHeader("X-Cache-Lookup", "HIT - Idempotent POST");
                response.getWriter().write(record.getResponseBody());
                return;
            }

            // 4. Se a chave for nova, processar e capturar a resposta
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
            try {
                filterChain.doFilter(request, responseWrapper);

                int status = responseWrapper.getStatus();
                // Apenas salvar no cache se for sucesso (2xx) ou erro de validação do cliente (4xx)
                if (status >= 200 && status < 500) {
                    byte[] responseArray = responseWrapper.getContentAsByteArray();
                    String responseBody = new String(responseArray, responseWrapper.getCharacterEncoding());

                    IdempotencyRecord newRecord = new IdempotencyRecord(key, status, responseBody);
                    repository.save(newRecord);
                }

                responseWrapper.copyBodyToResponse();
            } catch (Exception e) {
                responseWrapper.copyBodyToResponse();
                throw e;
            }
            
        } catch (Exception e) {
            // Em caso de qualquer erro imprevisto, fazemos o fallback de segurança deixando passar a requisição
            System.err.println(">>> [CRITICAL] Erro interno no IdempotencyFilter: " + e.getMessage());
            e.printStackTrace();
            filterChain.doFilter(request, response);
        }
    }
}
