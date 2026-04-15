# Copilot Instructions - ip-geolocation-service

## Sobre o Projeto

Este é um microserviço REST de geolocalização que identifica informações geográficas a partir de endereços IP. O projeto utiliza Clean Architecture com separação clara entre `application/` (regras de negócio) e `infrastructure/` (detalhes técnicos).

## Stack Tecnológica

- Java 21 (use Records, Pattern Matching, Text Blocks)
- Spring Boot 3.3.x
- Maven
- Caffeine (cache)
- JUnit 5 + Mockito + WireMock (testes)

## Convenções de Código

### Estrutura de Pacotes

```
com.example.geolocation.application.domain.model    → Entidades e Value Objects
com.example.geolocation.application.domain.exception → Exceções de domínio
com.example.geolocation.application.port.in         → Interfaces de casos de uso
com.example.geolocation.application.port.out        → Interfaces de serviços externos
com.example.geolocation.application.service         → Implementações dos use cases
com.example.geolocation.infrastructure.adapter.in.web → Controllers
com.example.geolocation.infrastructure.adapter.out.cache → Cache
com.example.geolocation.infrastructure.adapter.out.client → HTTP clients
com.example.geolocation.infrastructure.config       → Configurações Spring
com.example.geolocation.infrastructure.validation   → Validators
```

### Regras de Dependência

- `application` NUNCA depende de `infrastructure`
- Use interfaces (ports) para abstrair dependências externas
- Controllers usam ports.in, adapters implementam ports.out

### Naming

- Use `*UseCase` para input ports
- Use `*Provider`, `*Cache` para output ports
- Use `*Controller` para REST controllers
- Use `*Client` para HTTP clients
- Use `*Response`, `*Request` para DTOs

### Testes

- Nome: `*Test.java` para unit, `*IT.java` para integration
- Use `@DisplayName` na classe e métodos de teste
- Use `@Nested` para agrupar testes relacionados
- Use AAA pattern (Arrange, Act, Assert)
- Mock apenas ports.out nos serviços
- Use WireMock para testar HTTP clients

### Lombok Annotations

- `@RequiredArgsConstructor` + `@NonNull` para injeção com validação
- `@Getter` para exceções e enums com campos
- `@UtilityClass` para classes utilitárias estáticas
- `@Slf4j` para logging

### Spring Annotations

- `@Validated` em controllers para Bean Validation
- `@NotBlank` para parâmetros obrigatórios
- `@Schema` em DTOs para documentação Swagger

## Arquivos Importantes

- `docs/ARCHITECTURE.md` - Decisões arquiteturais
- `docs/STRATEGY.md` - Estratégias de implementação
- `docs/IMPLEMENTATION-PLAN.md` - Checklist de tarefas

## Referências

Consulte os prompts em `.github/prompts/` para boas práticas específicas:

- `java-spring-best-practices.prompt.md`
- `clean-architecture.prompt.md`
- `testing.prompt.md`
- `api-design.prompt.md`
