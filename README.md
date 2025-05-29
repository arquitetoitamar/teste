# Sistema de Gerenciamento de Estacionamento

Este é um sistema backend para gestão de estacionamentos, desenvolvido em Java com Spring Boot.

## Requisitos

- Java 17+
- Maven
- Docker
- Docker Compose

## Configuração

1. Clone o repositório

2. Inicie os serviços com Docker Compose:
```bash
docker-compose up -d
```
Isso irá iniciar:
- PostgreSQL na porta 5432
- Simulador de garagem

3. Execute a aplicação:
```bash
mvn spring-boot:run
```

O PostgreSQL estará disponível na porta 5432 e a aplicação em http://localhost:3003.

## Endpoints da API

### Configuração da Garagem
- `GET /garage` - Obtém a configuração atual da garagem
- `POST /garage` - Importa nova configuração da garagem

### Status
- `POST /plate-status` - Consulta status de uma placa
- `POST /spot-status` - Consulta status de uma vaga

### Webhook de Eventos
- `POST /webhook` - Recebe eventos da garagem

#### Tipos de Eventos

##### 1. Entrada na Garagem
```json
{
  "license_plate": "ZUL0001",
  "entry_time": "2025-01-01T12:00:00.000Z",
  "event_type": "ENTRY"
}
```

##### 2. Entrada na Vaga
```json
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}
```

##### 3. Saída da Garagem
```json
{
  "license_plate": "ZUL0001",
  "exit_time": "2025-01-01T12:00:00.000Z",
  "event_type": "EXIT"
}
```

### Exemplos de Resposta

#### Status da Placa
```json
{
  "license_plate": "ZUL0001",
  "price_until_now": 10.00,
  "entry_time": "2025-01-01T12:00:00.000Z",
  "time_parked": "2025-01-01T13:00:00.000Z",
  "lat": -23.561684,
  "lng": -46.655981
}
```

#### Status da Vaga
```json
{
  "occupied": true,
  "license_plate": "ZUL0001",
  "price_until_now": 10.00,
  "entry_time": "2025-01-01T12:00:00.000Z",
  "time_parked": "2025-01-01T13:00:00.000Z"
}
```

#### Configuração da Garagem
```json
{
  "garage": [
    {
      "sector": "A1",
      "base_price": 10.00,
      "max_capacity": 50,
      "open_hour": "06:00",
      "close_hour": "22:00",
      "duration_limit_minutes": 120
    }
  ],
  "spots": [
    {
      "sector": "A1",
      "lat": -23.561684,
      "lng": -46.655981
    }
  ]
}
```

## Regras de Negócio

### Preços Dinâmicos
- Preço base por setor
- Ajuste baseado na ocupação
- Limite de tempo por setor

### Ocupação da Garagem
- Controle de capacidade por setor
- Status de vagas em tempo real
- Histórico de eventos

## Estrutura do Projeto

```
src/main/java/com/estapar/parking/
├── controller/    # Controllers REST
├── dto/          # Data Transfer Objects
├── model/        # Entidades JPA
├── repository/   # Repositórios JPA
└── service/      # Serviços de negócio
```

## Testes

Para executar os testes:
```bash
mvn test
```

## Docker Compose

O arquivo `docker-compose.yml` configura dois serviços:

1. **PostgreSQL**
   - Porta: 5432
   - Banco: parking_management
   - Usuário: postgres
   - Senha: postgres
   - Volume persistente para dados

2. **Simulador de Garagem**
   - Usa a imagem cfontes0estapar/garage-sim:1.0.0
   - Modo de rede host para comunicação com a aplicação

Para gerenciar os containers:
```bash
# Iniciar serviços
docker-compose up -d

# Parar serviços
docker-compose down

# Ver logs
docker-compose logs -f

# Reiniciar serviços
docker-compose restart
```

## Documentação da API

A documentação Swagger/OpenAPI está disponível em:
- Swagger UI: http://localhost:3003/swagger-ui.html
- OpenAPI JSON: http://localhost:3003/api-docs

### Endpoints Principais

#### Configuração da Garagem
- `GET /garage` - Obtém configuração da garagem
- `POST /garage` - Importa configuração da garagem

#### Webhook do Simulador
- `POST /webhook` - Recebe eventos de entrada/saída/estacionamento

#### Consultas
- `POST /plate-status` - Consulta status de uma placa
- `POST /spot-status` - Consulta status de uma vaga
- `GET /revenue` - Consulta faturamento

## Regras de Negócio

### Preço Dinâmico
- Lotação < 25%: 10% de desconto
- Lotação < 50%: Preço base
- Lotação < 75%: 10% de aumento
- Lotação < 100%: 25% de aumento

### Lotação
- Com 100% de lotação, o setor é fechado
- Só permite entrada após saída de veículo

## Estrutura do Projeto

```
src/main/java/com/estapar/parking/
├── controller/    # Controllers REST
├── dto/          # Data Transfer Objects
├── model/        # Entidades JPA
├── repository/   # Repositórios JPA
└── service/      # Serviços de negócio
```

## Testes

Para executar os testes:
```bash
mvn test
```

## Docker Compose

O arquivo `docker-compose.yml` configura dois serviços:

1. **PostgreSQL**
   - Porta: 5432
   - Banco: parking_management
   - Usuário: postgres
   - Senha: postgres
   - Volume persistente para dados

2. **Simulador de Garagem**
   - Usa a imagem cfontes0estapar/garage-sim:1.0.0
   - Modo de rede host para comunicação com a aplicação

Para gerenciar os containers:
```bash
# Iniciar serviços
docker-compose up -d

# Parar serviços
docker-compose down

# Ver logs
docker-compose logs -f

# Reiniciar serviços
docker-compose restart
``` # teste
