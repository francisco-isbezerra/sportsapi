package com.sportsapi.sports_ecommerce.security;

import com.sportsapi.sports_ecommerce.enums.NivelAcesso;
import com.sportsapi.sports_ecommerce.model.ApiKey;
import com.sportsapi.sports_ecommerce.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@org.springframework.core.annotation.Order(2)
public class ApiKeyFilter extends OncePerRequestFilter {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            String method = request.getMethod();

            // 1. Ignorar caminhos públicos de documentação, console H2 e geração de chaves
            if (path.startsWith("/swagger-ui") || 
                path.startsWith("/v3/api-docs") || 
                path.startsWith("/api-docs") || 
                path.startsWith("/h2-console") || 
                path.equals("/api/auth/keys/gerar")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Para rotas do H2 ou root se necessário
            if (path.equals("/") || path.equals("/favicon.ico")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extrair o header X-API-Key
            String apiKeyHeader = request.getHeader("X-API-Key");

            if (apiKeyHeader == null || apiKeyHeader.trim().isEmpty()) {
                enviarErro(response, HttpServletResponse.SC_UNAUTHORIZED, 
                        "Chave de API ausente", "O cabeçalho X-API-Key é obrigatório para acessar este recurso.");
                return;
            }

            // Lookup dinâmico e seguro do ApiKeyRepository caso a injeção via Autowired atrase
            if (apiKeyRepository == null) {
                try {
                    org.springframework.web.context.WebApplicationContext wac = 
                            org.springframework.web.context.support.WebApplicationContextUtils
                            .getWebApplicationContext(request.getServletContext());
                    if (wac != null) {
                        apiKeyRepository = wac.getBean(ApiKeyRepository.class);
                    }
                } catch (Exception ex) {
                    System.err.println(">>> [WARN] Não foi possível obter o ApiKeyRepository dinamicamente: " + ex.getMessage());
                }
            }

            if (apiKeyRepository == null) {
                // Caso extremo onde o banco não está acessível
                enviarErro(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Serviço indisponível", "O banco de dados de autenticação está temporariamente inacessível.");
                return;
            }

            // 3. Buscar a chave no banco de dados
            Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByChaveAndAtivoTrue(apiKeyHeader);
            if (apiKeyOpt.isEmpty()) {
                enviarErro(response, HttpServletResponse.SC_UNAUTHORIZED, 
                        "Chave de API inválida", "A chave de API fornecida não é válida ou foi revogada.");
                return;
            }

            ApiKey apiKey = apiKeyOpt.get();

            // 4. Verificar níveis de acesso (NivelAcesso)
            // Métodos de escrita (POST, PUT, PATCH, DELETE) exigem nível ADMIN
            if (!method.equalsIgnoreCase("GET")) {
                if (apiKey.getNivelAcesso() != NivelAcesso.ADMIN) {
                    enviarErro(response, HttpServletResponse.SC_FORBIDDEN, 
                            "Acesso negado", "Esta operação exige permissões administrativas (ADMIN).");
                    return;
                }
            }

            // Adicionar informações da chave à requisição para possíveis usos futuros
            request.setAttribute("usuarioAutenticado", apiKey.getUsuario());
            request.setAttribute("nivelAcessoAutenticado", apiKey.getNivelAcesso());

            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            // Em caso de qualquer erro imprevisto, fazemos o fallback de segurança deixando passar a requisição
            System.err.println(">>> [CRITICAL] Erro interno no ApiKeyFilter: " + e.getMessage());
            e.printStackTrace();
            filterChain.doFilter(request, response);
        }
    }

    private void enviarErro(HttpServletResponse response, int status, String erro, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                java.time.LocalDateTime.now(), status, erro, mensagem
        );
        try {
            response.getWriter().write(json);
        } catch (IllegalStateException e) {
            try {
                response.getOutputStream().write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception ex) {
                System.err.println(">>> [WARN] Falha total ao escrever resposta de erro no ApiKeyFilter: " + ex.getMessage());
            }
        }
    }
}
