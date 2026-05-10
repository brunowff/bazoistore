# Baozi Store — API REST

API REST para gerenciamento de uma pequena loja de pãozinho chinês (baozi). Desenvolvida como Trabalho Prático da disciplina de **Desenvolvimento Web Back-End**, a aplicação cobre o ciclo básico de operação de uma loja: cadastro de clientes, catálogo de produtos e registro de pedidos.

---

## Sumário

- [Visão Geral](#visão-geral)
- [Stack Tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Estrutura de Pacotes](#estrutura-de-pacotes)
- [Modelo de Dados](#modelo-de-dados)
- [Endpoints da API](#endpoints-da-api)
- [Configuração do Banco de Dados](#configuração-do-banco-de-dados)
- [Como Executar](#como-executar)
- [Exemplos de Requisições](#exemplos-de-requisições)

---

## Visão Geral

A **Baozi Store API** expõe um conjunto de endpoints HTTP que permitem:

- Cadastrar, consultar, atualizar e remover **clientes**
- Cadastrar, consultar, atualizar e remover **produtos** do catálogo
- Registrar, consultar, atualizar e remover **pedidos**, associando um cliente a um produto com uma determinada quantidade

Toda a comunicação é feita em **JSON**. O banco de dados é **H2 em memória**, o que significa que os dados são recriados a cada inicialização da aplicação — ideal para desenvolvimento e testes.

---

## Stack Tecnológica

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 25 |
| Framework | Spring Boot | 4.0.6 |
| Web | Spring Web MVC | (gerenciado pelo Boot) |
| Persistência | Spring Data JPA + Hibernate | (gerenciado pelo Boot) |
| Banco de Dados | H2 Database (in-memory) | (gerenciado pelo Boot) |
| Build | Apache Maven (Maven Wrapper) | — |
| Testes | Spring Boot Starter Test | (gerenciado pelo Boot) |
| Dev | Spring Boot DevTools | (gerenciado pelo Boot) |

---

## Arquitetura

O projeto segue o padrão **MVC (Model-View-Controller)** adaptado para APIs REST, organizado em três camadas bem definidas:

```
Requisição HTTP (Postman / cliente)
        │
        ▼
┌───────────────────┐
│    Controller     │  ← Recebe a requisição, valida o método HTTP e delega
│  (REST Layer)     │    ao repositório. Retorna ResponseEntity com status HTTP.
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│   Repository      │  ← Interface JpaRepository. Gerada automaticamente pelo
│  (Data Layer)     │    Spring Data JPA. Sem código manual necessário.
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│     Model         │  ← Entidades JPA mapeadas para tabelas no H2.
│  (Domain Layer)   │    Contém apenas atributos, construtores e getters/setters.
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│   H2 Database     │  ← Banco relacional em memória. Tabelas criadas
│   (in-memory)     │    automaticamente pelo Hibernate no startup.
└───────────────────┘
```

Não há camada de Service explícita neste projeto — a lógica de negócio é simples o suficiente para que os Controllers se comuniquem diretamente com os Repositories, mantendo o código enxuto.

---

## Estrutura de Pacotes

```
src/
└── main/
    ├── java/com/bazoistore/store/
    │   ├── StoreApplication.java          # Ponto de entrada da aplicação
    │   ├── model/
    │   │   ├── Cliente.java               # Entidade JPA: Cliente
    │   │   ├── Produto.java               # Entidade JPA: Produto
    │   │   └── Pedido.java                # Entidade JPA: Pedido
    │   ├── repository/
    │   │   ├── ClienteRepository.java     # CRUD automático via JpaRepository
    │   │   ├── ProdutoRepository.java
    │   │   └── PedidoRepository.java
    │   └── controller/
    │       ├── ClienteController.java     # Endpoints REST /clientes
    │       ├── ProdutoController.java     # Endpoints REST /produtos
    │       └── PedidoController.java      # Endpoints REST /pedidos
    └── resources/
        └── application.properties         # Configurações do Spring Boot e H2
```

---

## Modelo de Dados

### Cliente

Representa um comprador cadastrado na loja.

| Campo | Tipo Java | Tipo SQL | Descrição |
|---|---|---|---|
| `id` | `Long` | BIGINT (PK, auto) | Identificador único gerado automaticamente |
| `nome` | `String` | VARCHAR | Nome completo do cliente |
| `clienteDesde` | `LocalDate` | DATE | Data de cadastro do cliente |

### Produto

Representa um item disponível no catálogo da loja.

| Campo | Tipo Java | Tipo SQL | Descrição |
|---|---|---|---|
| `id` | `Long` | BIGINT (PK, auto) | Identificador único gerado automaticamente |
| `nome` | `String` | VARCHAR | Nome do produto |
| `preco` | `BigDecimal` | DECIMAL | Preço unitário com precisão decimal |
| `estoque` | `Boolean` | BOOLEAN | `true` se disponível em estoque, `false` se esgotado |

### Pedido

Representa a compra de um produto por um cliente.

| Campo | Tipo Java | Tipo SQL | Descrição |
|---|---|---|---|
| `id` | `Long` | BIGINT (PK, auto) | Identificador único gerado automaticamente |
| `clienteId` | `Long` | BIGINT (FK) | Referência ao `id` do Cliente |
| `produtoId` | `Long` | BIGINT (FK) | Referência ao `id` do Produto |
| `quantidade` | `Integer` | INTEGER | Quantidade de unidades do produto no pedido |

### Diagrama Entidade-Relacionamento

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   CLIENTE    │         │    PEDIDO    │         │   PRODUTO    │
│──────────────│         │──────────────│         │──────────────│
│ PK id        │◄────────│ FK clienteId │         │ PK id        │
│    nome      │  1    N │ FK produtoId │────────►│    nome      │
│ clienteDesde │         │    quantidade│  N    1 │    preco     │
└──────────────┘         └──────────────┘         │    estoque   │
                                                   └──────────────┘
```

Um cliente pode ter vários pedidos. Cada pedido referencia exatamente um cliente e um produto.

---

## Endpoints da API

A API base roda em `http://localhost:8080`.

### Clientes — `/clientes`

| Método | Rota | Descrição | Status de Sucesso |
|---|---|---|---|
| `POST` | `/clientes` | Cria um novo cliente | `201 Created` |
| `GET` | `/clientes` | Lista todos os clientes | `200 OK` |
| `GET` | `/clientes/{id}` | Retorna um cliente pelo ID | `200 OK` |
| `PUT` | `/clientes/{id}` | Atualiza os dados de um cliente | `200 OK` |
| `DELETE` | `/clientes/{id}` | Remove um cliente pelo ID | `204 No Content` |

### Produtos — `/produtos`

| Método | Rota | Descrição | Status de Sucesso |
|---|---|---|---|
| `POST` | `/produtos` | Cria um novo produto | `201 Created` |
| `GET` | `/produtos` | Lista todos os produtos | `200 OK` |
| `GET` | `/produtos/{id}` | Retorna um produto pelo ID | `200 OK` |
| `PUT` | `/produtos/{id}` | Atualiza os dados de um produto | `200 OK` |
| `DELETE` | `/produtos/{id}` | Remove um produto pelo ID | `204 No Content` |

### Pedidos — `/pedidos`

| Método | Rota | Descrição | Status de Sucesso |
|---|---|---|---|
| `POST` | `/pedidos` | Cria um novo pedido | `201 Created` |
| `GET` | `/pedidos` | Lista todos os pedidos | `200 OK` |
| `GET` | `/pedidos/{id}` | Retorna um pedido pelo ID | `200 OK` |
| `PUT` | `/pedidos/{id}` | Atualiza os dados de um pedido | `200 OK` |
| `DELETE` | `/pedidos/{id}` | Remove um pedido pelo ID | `204 No Content` |

### Códigos de Resposta HTTP

| Código | Significado |
|---|---|
| `200 OK` | Requisição bem-sucedida com corpo de resposta |
| `201 Created` | Recurso criado com sucesso |
| `204 No Content` | Operação bem-sucedida sem corpo de resposta (DELETE) |
| `404 Not Found` | Recurso não encontrado para o ID informado |

---

## Configuração do Banco de Dados

O banco H2 roda inteiramente em memória (`jdbc:h2:mem:baozidb`). As tabelas são criadas automaticamente pelo Hibernate no startup (`ddl-auto=create-drop`) e destruídas ao encerrar a aplicação.

```properties
spring.datasource.url=jdbc:h2:mem:baozidb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

O console web do H2 fica disponível em `http://localhost:8080/h2-console` enquanto a aplicação estiver rodando. Use as credenciais acima para conectar.

---

## Como Executar

**Pré-requisitos:** Java 25 e Maven instalados (ou use o Maven Wrapper incluído no projeto).

```bash
# Clonar o repositório
git clone git@github.com:brunowff/bazoistore.git
cd bazoistore

# Executar com Maven Wrapper
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

---

## Exemplos de Requisições

Todos os exemplos abaixo usam `Content-Type: application/json`.

### Criar um Cliente

```http
POST /clientes
Content-Type: application/json

{
  "nome": "Bruno RU123456",
  "clienteDesde": "2026-05-10"
}
```

Resposta `201 Created`:
```json
{
  "id": 1,
  "nome": "Bruno RU123456",
  "clienteDesde": "2026-05-10"
}
```

### Criar um Produto

```http
POST /produtos
Content-Type: application/json

{
  "nome": "Baozi Tradicional",
  "preco": 8.50,
  "estoque": true
}
```

Resposta `201 Created`:
```json
{
  "id": 1,
  "nome": "Baozi Tradicional",
  "preco": 8.50,
  "estoque": true
}
```

### Criar um Pedido

```http
POST /pedidos
Content-Type: application/json

{
  "clienteId": 1,
  "produtoId": 1,
  "quantidade": 3
}
```

Resposta `201 Created`:
```json
{
  "id": 1,
  "clienteId": 1,
  "produtoId": 1,
  "quantidade": 3
}
```

### Listar todos os Clientes

```http
GET /clientes
```

### Buscar Produto por ID

```http
GET /produtos/1
```

### Atualizar um Cliente

```http
PUT /clientes/1
Content-Type: application/json

{
  "nome": "Bruno Atualizado RU123456",
  "clienteDesde": "2026-01-01"
}
```

### Deletar um Pedido

```http
DELETE /pedidos/1
```

Resposta `204 No Content` (sem corpo).
