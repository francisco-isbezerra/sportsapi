# Sports Ecommerce API 🏆

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Render](https://img.shields.io/badge/Render-%46E3B7.svg?style=for-the-badge&logo=render&logoColor=white)

API robusta desenvolvida para a disciplina de **DESENVOLVIMENTO DE WEB SERVICES (4º Semestre)**. O sistema gerencia um ecossistema de e-commerce esportivo, focando em escalabilidade, padronização RESTful e documentação automatizada.

---

## 🚀 Acesso Rápido à Documentação (Swagger)

A API está publicada e pode ser testada interativamente através do link abaixo:

🔗 **[Documentação Swagger UI - Sports Ecommerce](https://sportsapi-dg1j.onrender.com/swagger-ui/index.html)**
*(Nota: Por ser hospedagem gratuita, o primeiro carregamento pode levar até 30 segundos para "despertar" o servidor).*

---

## 🛠️ Stack Tecnológica

- **Linguagem:** Java 17 (OpenJDK)
- **Framework:** Spring Boot 3.x
- **Persistência:** Spring Data JPA / Hibernate
- **Banco de Dados:** H2 Database (In-Memory para testes ágeis)
- **Documentação:** OpenAPI 3 / Swagger
- **Containerização:** Docker & Docker Compose
- **Deploy:** Render Cloud

---

## 🏛️ Arquitetura e Padrões de Projeto

O projeto foi desenhado seguindo as melhores práticas de engenharia de software:

1.  **MVC (Model-View-Controller):** Separação clara entre a lógica de negócio, persistência e exposição de endpoints.
2.  **Maturidade de Richardson (Nível 3):** Implementação completa de **HATEOAS**, permitindo que o cliente navegue na API através de links hipermídia.
3.  **Idempotência:** Aplicação de `Idempotency-Key` em métodos não-idempotentes (POST) conforme requisitos de segurança.
4.  **Versionamento de API:** Uso de prefixos `/v1/` nas URLs para garantir evolução sem quebra de contrato.
5.  **Global Exception Handling:** Tratamento centralizado de erros com respostas padronizadas em JSON para os status 400, 404 e 500.

---

## 📝 Regras de Negócio Implementadas

A API gerencia cinco entidades core com os seguintes comportamentos:

- **Produtos:** Cadastro de equipamentos com validação de preço positivo e classificação por condição (Novo/Usado) via Enums.
- **Categorias:** Organização lógica de produtos com relacionamento One-to-Many.
- **Clientes & Endereços:** Relacionamento One-to-One com persistência em cascata (ao salvar um cliente, o endereço é processado automaticamente).
- **Pedidos:** Entidade transacional que conecta clientes a múltiplos produtos (Many-to-Many), registrando a data e hora exata da venda.

---

## 📡 Endpoints Disponíveis

Todos os recursos principais suportam os 5 verbos HTTP fundamentais:

| Método | Descrição | Idempotente |
| :--- | :--- | :--- |
| `GET` | Recuperação de dados (Listagem paginada e busca por ID) | Sim |
| `POST` | Criação de novos recursos | Não |
| `PUT` | Atualização integral de um recurso existente | Sim |
| `PATCH` | Atualização parcial (ex: alterar apenas o preço) | Não |
| `DELETE` | Remoção lógica/física do recurso | Sim |

---

## 🐳 Como rodar localmente com Docker

Caso deseje executar o projeto em ambiente local:

```bash
# Clone o repositório
git clone https://github.com/francisco-isbezerra/sportsapi.git

# Entre na pasta
cd sports-ecommerce

# Execute o container
docker-compose up --build