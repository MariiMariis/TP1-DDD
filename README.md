# E-commerce legado — atividade de DDD

Aplicação monolítica propositalmente acoplada para uma atividade de refatoração.

## Tecnologias

- Java 25
- Spring Boot 4.1.0
- Maven
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 em memória

## Requisitos

- JDK 25
- Maven 3.6.3 ou superior

## Executar

```bash
mvn spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

## Console H2

```text
http://localhost:8080/h2-console
```

Dados da conexão:

```text
JDBC URL: jdbc:h2:mem:ecommerce
User Name: sa
Password:
```

## Fluxo principal

Use o arquivo `requests.http` ou execute:

```bash
curl -X POST http://localhost:8080/pedidos   -H "Content-Type: application/json"   -d '{
    "usuarioId": 1,
    "itens": [
      {
        "produtoId": 1,
        "quantidade": 2
      }
    ],
    "formaPagamento": "CARTAO",
    "numeroCartao": "4111111111111111"
  }'
```

## Regras simuladas de pagamento

- Valor menor ou igual a zero: recusado.
- Valor acima de R$ 10.000,00: recusado por limite.
- Cartão terminado em `0000`: cartão bloqueado.
- Cartão terminado em `1111`: aprovado.
- Outros cartões: aprovados quando o valor for válido.

## Aviso pedagógico

A arquitetura foi intencionalmente construída com problemas:

- organização horizontal por camada técnica;
- entidades JPA usadas diretamente nos controllers;
- relacionamentos entre entidades de contextos diferentes;
- `PedidoService` com múltiplas responsabilidades;
- acesso direto a vários repositórios;
- pagamento acoplado a pedido e usuário;
- dependência de um processador concreto;
- regras distribuídas em services;
- uma única transação envolvendo pedido, estoque e pagamento;
- ausência de Aggregate Root, Value Objects, portas e adaptadores.

Esses problemas fazem parte da atividade e não devem ser corrigidos antes da entrega aos alunos.

---

## Refatoração realizada — Bounded Context de Pagamento

Foi criado um contexto de Pagamento isolado dentro do mesmo projeto Spring Boot, aplicando os conceitos de Domain-Driven Design.

### O que foi implementado

- **Pagamento como Aggregate Root** com factory method `criar()` que encapsula todas as regras de aprovação e recusa.
- **Value Objects**: `Dinheiro` (valor monetário), `NumeroCartao` (validação e mascaramento) e `PagamentoId` (identidade).
- **Enums**: `StatusPagamento` (APROVADO, RECUSADO, BLOQUEADO) e `FormaPagamento` (CARTAO), substituindo strings soltas.
- **Regras de negócio encapsuladas** no Aggregate Root, não mais espalhadas em services.
- **Serviço de aplicação** (`PagamentoApplicationService`) que orquestra a criação do agregado, o processamento e a persistência.
- **Interface de repositório do domínio** (`PagamentoRepository`) como porta, sem dependência de Spring Data.
- **Implementação JPA separada**: `PagamentoJpaEntity` mapeada para a tabela `pagamentos`, usando apenas `pedido_id` e `usuario_id` como colunas Long — sem `@OneToOne` ou `@ManyToOne` para entidades externas.
- **Adapter** (`PagamentoRepositoryAdapter`) que implementa a porta do domínio e converte entre o agregado e a entidade JPA.
- **Abstração do processador de cartão**: interface `ProcessadorCartao` com implementação concreta `ProcessadorCartaoSimulado`.
- **Interface de integração** (`PagamentoFacade`) que serve como único ponto de contato entre o contexto de Pedido e o contexto de Pagamento.

### Isolamento alcançado

O contexto de Pagamento **não acessa diretamente**:

- `UsuarioRepository` — recebe apenas `usuarioId` (Long)
- `ProdutoRepository` — não necessita de produtos
- `EstoqueRepository` — não necessita de estoque
- `PedidoRepository` — recebe apenas `pedidoId` (Long)

### Regras de pagamento preservadas

Todas as regras originais continuam funcionando:

- Valor menor ou igual a zero: recusado (`VALOR_INVALIDO`)
- Valor acima de R$ 10.000,00: recusado (`LIMITE_EXCEDIDO`)
- Cartão terminado em `0000`: bloqueado (`CARTAO_BLOQUEADO`)
- Cartão terminado em `1111`: aprovado
- Demais cartões válidos: aprovados

### Estrutura de pacotes do contexto

```
br.edu.infnet.ecommerce.pagamento
├── domain/
│   ├── Pagamento.java              (Aggregate Root)
│   ├── Dinheiro.java               (Value Object)
│   ├── NumeroCartao.java           (Value Object)
│   ├── PagamentoId.java            (Value Object)
│   ├── StatusPagamento.java        (enum)
│   ├── FormaPagamento.java         (enum)
│   └── port/
│       ├── PagamentoRepository.java  (interface)
│       ├── ProcessadorCartao.java    (interface)
│       └── ResultadoProcessadorCartao.java
├── application/
│   ├── PagamentoApplicationService.java
│   ├── ProcessarPagamentoCommand.java
│   └── ResultadoPagamento.java
├── infrastructure/
│   ├── persistence/
│   │   ├── PagamentoJpaEntity.java
│   │   ├── PagamentoJpaRepository.java
│   │   └── PagamentoRepositoryAdapter.java
│   └── gateway/
│       └── ProcessadorCartaoSimulado.java
└── integration/
    ├── PagamentoFacade.java         (interface pública)
    └── PagamentoFacadeImpl.java
```

### Alterações no legado

- `PedidoService` passou a usar `PagamentoFacade` no lugar de `PagamentoService` e `PagamentoRepository`.
- Removidos: `PagamentoService`, `Pagamento` (entity), `ProcessadorPagamento`, `ResultadoProcessamento` e `PagamentoRepository` (legado).

---

## v2.0.0 — Eventos de Domínio

### Agregado que referencia outro agregado

O Aggregate Root `Pagamento` referencia o agregado `Pedido` e a entidade `Usuario` apenas por seus identificadores (`pedidoId` e `usuarioId` do tipo `Long`), sem dependência direta das classes JPA de outros contextos:

```java
public class Pagamento {
    private PagamentoId id;
    private final Long pedidoId;   // referência ao agregado Pedido
    private final Long usuarioId;  // referência ao agregado Usuario
    // ...
}
```

### Abstração de evento de domínio

A classe abstrata `EventoDominio` serve como base para todos os eventos do sistema. Cada evento possui um identificador único e um timestamp:

```java
public abstract class EventoDominio {
    private final String eventId;
    private final LocalDateTime ocorridoEm;
    public abstract String tipo();
}
```

### Implementação de evento de domínio

Dois eventos concretos foram implementados:

- `PagamentoAprovadoEvent` — publicado quando o pagamento é aprovado
- `PagamentoRecusadoEvent` — publicado quando o pagamento é recusado ou bloqueado

### Método de negócio com publicação de evento

O factory method `Pagamento.criar()` registra internamente um evento de domínio conforme o resultado do processamento:

```java
public static Pagamento criar(...) {
    // regras de negócio...
    Pagamento p = new Pagamento(...);
    p.registrarEvento(new PagamentoAprovadoEvent(
        null, pedidoId, usuarioId, valor.valor(), resultado.codigoAutorizacao()
    ));
    return p;
}
```

O `PagamentoApplicationService` coleta os eventos do agregado após a persistência e os publica via `ApplicationEventPublisher` do Spring.
