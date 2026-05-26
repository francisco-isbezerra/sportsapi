package com.sportsapi.sports_ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sports Ecommerce API 🏆")
                        .description("Esta API gerencia um ecossistema completo de e-commerce esportivo, desenvolvida como Projeto Final para a disciplina de Desenvolvimento de APIs com Spring Boot. \n\n" +
                                "O sistema foi projetado para ser simples de entender e testar, organizando os recursos em três pilares principais logo abaixo:\n" +
                                "* **Autenticação (Chaves de API):** Onde você gerencia o acesso seguro. Para testar as demais rotas, clique no botão **Authorize** no topo da página e utilize a chave de testes: `sports-admin-test-key`.\n" +
                                "* **Endereços:** Gerenciamento completo de localizações com suporte a links dinâmicos (HATEOAS).\n" +
                                "* **Produtos:** Catálogo de equipamentos esportivos com suporte a paginação de dados e controle de versões (X-API-Version).\n\n" +
                                "**Recursos Avançados Implementados:**\n" +
                                "* 🔐 **Segurança:** Autenticação via header `X-API-Key` e controle de requisições por IP (Rate Limiting).\n" +
                                "* 🔄 **Idempotência:** Proteção contra cliques duplos em rotas de criação (POST) usando `X-Idempotency-Key`.\n" +
                                "* 📄 **Navegabilidade:** Paginação em todas as listagens e links HATEOAS para guiar o cliente."))
                .addSecurityItem(new SecurityRequirement()
                        .addList("ApiKeyAuth")
                        .addList("ApiVersionAuth"))
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                                .name("X-API-Key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Insira a chave de API de testes (ex: sports-admin-test-key) para autenticação."))
                        .addSecuritySchemes("ApiVersionAuth", new SecurityScheme()
                                .name("X-API-Version")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Insira o cabeçalho opcional de versão (ex: 1 ou 2) para testar os endpoints versionados.")));
    }

    @Bean
    public OperationCustomizer addVersionHeaderParameter() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new HeaderParameter()
                    .name("X-API-Version")
                    .description("Versão da API (ex: 1 ou 2)")
                    .required(false)
                    .schema(new StringSchema()._default("1")));
            return operation;
        };
    }
}

