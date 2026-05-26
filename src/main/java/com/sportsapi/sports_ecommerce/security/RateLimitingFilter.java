package com.sportsapi.sports_ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@org.springframework.core.annotation.Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, TokenBucket> ipBuckets = new ConcurrentHashMap<>();

    private static final int BUCKET_CAPACITY = 15;
    private static final double REFILL_RATE_PER_SECOND = 2.0; // Recarrega 2 tokens por segundo

    private static class TokenBucket {
        private final double capacity;
        private double tokens;
        private long lastRefillTime;

        public TokenBucket(double capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume(double refillRatePerSecond, int[] remainingTokensOut, long[] retryAfterMsOut) {
            long now = System.currentTimeMillis();
            long elapsedMs = now - lastRefillTime;
            
            // Recarrega tokens proporcionalmente ao tempo decorrido
            double refillAmount = elapsedMs * (refillRatePerSecond / 1000.0);
            tokens = Math.min(capacity, tokens + refillAmount);
            lastRefillTime = now;

            if (tokens >= 1.0) {
                tokens -= 1.0;
                remainingTokensOut[0] = (int) Math.floor(tokens);
                return true;
            } else {
                remainingTokensOut[0] = 0;
                // Tempo necessário até ter pelo menos 1 token disponível
                double tokensNeeded = 1.0 - tokens;
                retryAfterMsOut[0] = (long) Math.ceil(tokensNeeded / (refillRatePerSecond / 1000.0));
                return false;
            }
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.beans.factory.annotation.Qualifier("handlerExceptionResolver")
    private org.springframework.web.servlet.HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Ignorar console H2 e documentação Swagger do rate limiting para facilitar desenvolvimento/testes
        if (path.startsWith("/swagger-ui") || 
            path.startsWith("/v3/api-docs") || 
            path.startsWith("/api-docs") || 
            path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Identificar cliente pelo IP
        String clientIp = obterIpCliente(request);

        // 3. Obter ou criar balde de tokens para este IP
        TokenBucket bucket = ipBuckets.computeIfAbsent(clientIp, ip -> new TokenBucket(BUCKET_CAPACITY, REFILL_RATE_PER_SECOND));

        int[] remainingTokensOut = new int[1];
        long[] retryAfterMsOut = new long[1];

        // 4. Tentar consumir token
        boolean allowed = bucket.tryConsume(REFILL_RATE_PER_SECOND, remainingTokensOut, retryAfterMsOut);

        // Definir cabeçalhos padrão do limite de taxa
        response.setHeader("X-RateLimit-Limit", String.valueOf(BUCKET_CAPACITY));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokensOut[0]));

        if (allowed) {
            filterChain.doFilter(request, response);
        } else {
            long retryAfterSeconds = (long) Math.ceil(retryAfterMsOut[0] / 1000.0);
            if (retryAfterSeconds < 1) retryAfterSeconds = 1;

            resolver.resolveException(request, response, null, 
                    new com.sportsapi.sports_ecommerce.exception.RateLimitExceededException(retryAfterSeconds));
        }
    }

    private String obterIpCliente(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
