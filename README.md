# Sports Ecommerce API 🏆

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/maven-%23C71A36.svg?style=for-the-badge&logo=apache-maven&logoColor=white)

API de alta performance desenvolvida para o projeto final de avaliação da disciplina de **DESENVOLVIMENTO DE WEB SERVICES (4º Semestre)**. O sistema gerencia um e-commerce de artigos esportivos completo, integrando as melhores práticas do padrão RESTful, hipermídia HATEOAS, paginação, segurança por chave de API, controle de concorrência por idempotência e limitação de taxa (Rate Limiting).

---

## 🚀 Como Executar o Projeto Localmente

1. Abra o terminal na pasta raiz do projeto.
2. Certifique-se de usar o Java 17 ou superior e execute o servidor através do Maven Wrapper:
   ```bash
   .\mvnw spring-boot:run
   ```
3. O servidor subirá na porta **8080**:
   - **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (Documentação interativa completa)
   - **Banco de Dados H2**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:sportsdb`, User: `sa`, Senha em branco)

---

## 🔑 Chaves de API Pré-configuradas para Testes (X-API-Key)

O sistema conta com um mecanismo de segurança em todas as rotas de negócio. Para testar a API no Swagger ou no Postman, utilize uma das chaves geradas automaticamente na inicialização:

*   **Chave ADMIN (Acesso Total)**: `sports-admin-test-key` *(Permite operações GET, POST, PUT, PATCH, DELETE)*
*   **Chave CLIENTE (Leitura Apenas)**: `sports-client-test-key` *(Permite apenas operações GET)*

---

## 🛠️ Stack Tecnológica e Requisitos Técnicos Atendidos

*   **Java 17 & Spring Boot 3.5.x**
*   **H2 Database (In-Memory)** para testes rápidos de persistência.
*   **Spring Data JPA** para ORM.
*   **Spring HATEOAS** com suporte a `EntityModel` e `PagedModel`.
*   **Springdoc OpenAPI (Swagger)** para documentação.
*   **Bean Validation** robusto com validação em cascata (`@Valid`).

---

## 🏛️ Arquitetura e Recursos Avançados Implementados (Parte 1 & Parte 2)

### 1. Modelagem Limpa e Sem Loops (Richardson Nível 3)
- **Eliminação de Recursão**: Resolvidos os loops de serialização JSON comuns ao usar relacionamentos bidirecionais do Hibernate com a anotação `@JsonIgnoreProperties`.
- **HATEOAS Completo**: Cada retorno individual possui links dinâmicos e autocontidos para `self`, `update`, `patch`, `delete` e `colecao`.
- **Paginação Real**: Listagens usam `PagedResourcesAssembler` que convertem os resultados em `PagedModel`, injetando links de navegação automática (`first`, `prev`, `self`, `next`, `last`).

### 2. Consultas Personalizadas por Entidade
Implementado pelo menos um endpoint de consulta personalizada paginada para cada entidade:
- **Categorias**: Buscar por nome contendo (case-insensitive).
- **Produtos**: Buscar por faixa de preço mínima e máxima.
- **Clientes**: Buscar por nome contendo (case-insensitive).
- **Endereços**: Buscar por CEP exato.
- **Pedidos**: Filtrar todas as vendas de um cliente específico.

### 3. Idempotência em POST (`X-Idempotency-Key`)
Implementado o `IdempotencyFilter` que exige e valida chaves idempotentes no cabeçalho. POSTs de cadastro de entidades só são processados uma vez por chave. Submissões duplicadas da mesma chave retornam a resposta cacheada sem executar a lógica de persistência novamente no banco.

### 4. Rate Limiting por IP (Token Bucket)
Desenvolvido um filtro de controle de tráfego (`RateLimitingFilter`) que monitora as chamadas por IP de origem:
- **Capacidade**: 15 requisições em uma janela deslizante com recarga gradual de 2 tokens por segundo.
- **Headers de Taxa**: Injeta `X-RateLimit-Limit` e `X-RateLimit-Remaining`.
- **Estouro de Limite**: Retorna status `429 Too Many Requests` com o cabeçalho **Retry-After** especificando o tempo de espera necessário.

### 5. Versionamento de API por Cabeçalho (`X-API-Version`)
Implementado no endpoint de listagem de produtos. A API assume a **V1** como padrão (caso o cabeçalho não seja enviado) e responde no formato normal. Caso seja enviado `X-API-Version=2`, a API aciona uma rota de novo contrato que retorna um DTO customizado (`ProdutoV2Dto`), simplificando a resposta com preço pré-formatado em Real e nome da categoria plano.

### 6. CORS & Exceções Globais
- **CORS**: Configurado detalhadamente para liberar cabeçalhos personalizados e expor os cabeçalhos de controle.
- **Global Exception Handler**: Captura erros de validação (@Valid), JSONs malformados, concorrência e restrições de integridade no banco (ex: e-mail duplicado) com retornos amigáveis.

---

## 📂 Material de Entrega Adicional

A coleção do Postman para testes está incluída na raiz do repositório:
*   [sports-ecommerce-api.postman_collection.json](file:///c:/Users/Irani/OneDrive/Documentos/01.%20Faculdade/4%C2%BA%20semestre/DESENVOLVIMENTO%20DE%20WEB%20SERVICES/Projetos%20Intelij/sports-ecommerce/sports-ecommerce-api.postman_collection.json)