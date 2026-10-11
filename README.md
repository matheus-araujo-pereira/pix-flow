<div align="center">

# ⚡ PixFlow: Event-Driven Payment & Settlement Engine

**Motor Transacional de Alta Concorrência para Emissão, Liquidação Assíncrona e Notificação de Pagamentos Instantâneos (PIX)**

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3+](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18.6%20Serverless-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![AWS SQS](https://img.shields.io/badge/AWS_SQS-Event--Driven-FF9900?style=for-the-badge&logo=amazonsqs&logoColor=white)](https://aws.amazon.com/sqs/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Terraform](https://img.shields.io/badge/Terraform-1.8+-7B42BC?style=for-the-badge&logo=terraform&logoColor=white)](https://www.terraform.io/)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)
[![JaCoCo Coverage](https://img.shields.io/badge/JaCoCo-100%25%20Coverage-brightgreen?style=for-the-badge&logo=codecov&logoColor=white)](https://www.jacoco.org/)
[![Vercel](https://img.shields.io/badge/Frontend-Vercel_Live-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://frontend-hazel-six-05z2k7m6sk.vercel.app)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

<br/>

[🚀 Live Demo Web na Vercel](https://frontend-hazel-six-05z2k7m6sk.vercel.app) • [📖 Swagger / OpenAPI Docs](https://pix-flow-backend.onrender.com/swagger-ui.html) • [📄 Manual de Testes (PDF)](docs/MANUAL_DE_TESTES_PIXFLOW.pdf) • [🏗️ Arquitetura Técnica](#-arquitetura-do-sistema) • [🧪 Manual de Testes](#-manual-de-testes--homologação) • [☁️ Deploy 100% Gratuito](#-guia-de-deploy-100-gratuito)

</div>

---

## 📌 Visão Geral do Projeto

O **PixFlow** é uma solução de engenharia corporativa concebida para simular a infraestrutura de mensageria, liquidação em lote e processamento em tempo real de arranjos de pagamento instantâneos (padrão BACEN / SPI).

Projetado sob os princípios de **Clean Architecture (Arquitetura Hexagonal)** e **Domain-Driven Design (DDD)**, o motor desacopla a recepção síncrona de requisições de pagamento da efetiva compensação bancária através de filas assíncronas no **Amazon SQS** (ou emuladas localmente via **LocalStack**), garantindo tempos de resposta de borda na casa de dezenas de milissegundos e tolerância total a falhas com Dead-Letter Queues (DLQ).

### 🎯 Principais Diferenciais de Engenharia
- **Clean Architecture & Hexagonal Ports**: Separação rigorosa entre `domain`, `application` (use cases) e `infrastructure` (adapters web, mensageria e persistência).
- **Validação Canônica Módulo 11**: Algoritmos matemáticos estritos para validação de CPF e CNPJ (com rejeição de sequências e dígitos verificadores incorretos), RFC 5322 para e-mail e E.164 para telefone.
- **Validação de Invariantes & Padrão BACEN**: Emissão de identificador `EndToEndId` padrão Banco Central (32 caracteres alfanuméricos com ISPB e timestamp) e geração em código do payload TLV EMVCo BR Code (Tag 00 a 63 com cálculo de checksum CRC-16/CCITT-FALSE).
- **Processamento em Lote Concorrente (Stress Lab)**: Endpoint `/api/v1/charges/batch` capaz de executar dezenas de liquidações simultâneas em threads paralelas e calcular métricas de Throughput (TPS).
- **Webhooks com Assinatura Criptográfica HMAC-SHA256**: Despacho de eventos `PIX.SETTLED` e `PIX.FAILED` com cabeçalho `X-Signature-SHA256` para garantia de autenticidade e integridade no destino.
- **Linha do Tempo e Auditoria Imutável**: Endpoint `/api/v1/charges/{id}/timeline` detalhando cada etapa do worker (`CREATED` ➔ `SQS_ENQUEUED` ➔ `SETTLEMENT_STARTED` ➔ `SETTLED`) e exportação completa do livro-razão em CSV.
- **DevOps & Portabilidade Híbrida**: Provisionamento declarativo via **Terraform** compatível tanto com **AWS Free Tier** quanto com **LocalStack**, viabilizando hospedagem em nuvem **100% gratuita no Render, Neon e Vercel**.
- **Engenharia de Qualidade**: 141 testes automatizados (unitários de domínio, validação canônica Módulo 11, serviços, controllers WebMvc com MockMvc e workers assíncronos) atingindo **100% de cobertura de código (3.427/3.427 instruções e 173/173 branches) verificada via JaCoCo** na esteira de CI/CD.

---

## 🏛️ Arquitetura do Sistema

### 1. Topologia de Componentes e Fluxo de Mensageria

```mermaid
graph TD
    subgraph ClientLayer["🖥️ Camada de Interface & Clientes"]
        SPA["Frontend SPA (React 18 + Vite + Tailwind CSS)<br/>Design Minimalista Austero na Vercel"]
        CLI["Cliente Terminal / cURL / Postman"]
    end

    subgraph ApiGateway["🚀 Core Backend (Spring Boot 3.3.4 - Java 21)"]
        direction TB
        Controller["ChargeController & BatchController<br/>Inbound Ports: Hexagonal Architecture"]
        Domain["Domain Engine (Cpf/Cnpj Módulo 11 + BR Code)<br/>Geração EMVCo CRC-16 & EndToEndId"]
        AppService["ChargeApplicationService<br/>Orquestração de Casos de Uso & Auditoria"]
        EventAdapter["Event Publisher Adapter<br/>Outbound Port: ChargeEventPublisherPort"]
        WebhookDispatcher["Webhook Dispatcher Service<br/>Assinatura HMAC-SHA256"]
    end

    subgraph StorageLayer["🗄️ Camada de Dados (PostgreSQL 18.6)"]
        DB[(PostgreSQL Database<br/>Neon Serverless / Docker Compose)]
        Flyway["Flyway Migrations (V1 e V2)"]
        AuditTable["Tabela de Auditoria (settlement_audit_logs)"]
        WebhookTable["Tabela de Webhooks (webhook_deliveries)"]
    end

    subgraph MessagingLayer["⚡ Camada Assíncrona & Mensageria"]
        SQS["AWS SQS: pix-settlement-queue<br/>(LocalStack / AWS Cloud)"]
        DLQ["AWS SQS DLQ: pix-settlement-dlq<br/>(MaxReceiveCount = 3)"]
        Workers["Async Settlement Workers<br/>Long Polling & Compensation Engine"]
    end

    SPA -->|HTTPS / JSON REST| Controller
    CLI -->|HTTP / JSON| Controller
    Controller --> AppService
    AppService --> Domain
    AppService -->|Persiste Estado PENDING| DB
    AppService -->|Dispara Evento de Liquidação| EventAdapter
    EventAdapter -->|Enqueue Event| SQS
    SQS -->|Redrive após 3 falhas| DLQ
    SQS -->|Pull Message / Long Polling| Workers
    Workers -->|PENDING -> PROCESSING -> SETTLED| DB
    Workers -->|Grava Latência & Metadados| AuditTable
    Workers -->|Dispara Notificação Assíncrona| WebhookDispatcher
    WebhookDispatcher -->|Persiste Histórico de Disparos| WebhookTable
```

### 2. Diagrama de Sequência: Ciclo de Liquidação e Webhook HMAC

```mermaid
sequenceDiagram
    autonumber
    actor User as Cliente / Pagador
    participant UI as React SPA (Vercel)
    participant API as Spring Boot API (Render)
    participant DB as PostgreSQL (Neon)
    participant SQS as AWS SQS Queue
    participant Worker as Settlement Worker
    participant ERP as Webhook Destino (ERP)

    User->>UI: Informa Chave PIX e Valor (R$ 150,00)
    UI->>UI: Validação local Módulo 11 (Feedback instantâneo)
    UI->>API: POST /api/v1/charges
    activate API
    API->>API: Valida Invariantes (CPF/CNPJ Módulo 11)
    API->>API: Gera EndToEndId BACEN & Payload BR Code
    API->>DB: INSERT charges (Status: PENDING)
    API->>SQS: SendMessage(chargeId, txId, amount)
    API-->>UI: 201 Created (ChargeResponse + QR Code EMV)
    deactivate API
    UI-->>User: Exibe QR Code e Código Copia-e-Cola

    par Processamento Assíncrono em Background
        Worker->>SQS: ReceiveMessages(WaitTimeSeconds=5)
        activate Worker
        SQS-->>Worker: Mensagem de Liquidação (chargeId)
        Worker->>DB: UPDATE charges SET status = 'PROCESSING'
        Worker->>DB: INSERT settlement_audit_logs (PROCESSING)
        Worker->>Worker: Simula Round-trip Rede SPI / BACEN (600ms - 1500ms)
        alt Liquidação Aprovada
            Worker->>DB: UPDATE charges SET status = 'SETTLED', settled_at = NOW()
            Worker->>DB: INSERT settlement_audit_logs (SETTLED)
            Worker->>ERP: POST /webhook (Payload JSON + X-Signature-SHA256)
            Worker->>DB: INSERT webhook_deliveries (Status: 200, Latency)
        else Falha ou Cancelamento
            Worker->>DB: UPDATE charges SET status = 'FAILED'
        end
        Worker->>SQS: DeleteMessage(receiptHandle)
        deactivate Worker
    end

    UI->>API: Polling / Auto-Sync GET /api/v1/charges
    API->>DB: SELECT * FROM charges ORDER BY created_at DESC
    DB-->>API: Transação atualizada
    API-->>UI: Transação com Status: SETTLED (Verde)
    UI-->>User: Atualização em Tempo Real na Interface
```

---

## 🗂️ Estrutura de Diretórios do Projeto

```text
pix-flow/
├── .github/
│   └── workflows/
│       └── ci-cd.yml                # Esteira automatizada (Testes, JaCoCo, Node, Terraform, Docker)
├── docs/
│   ├── MANUAL_DE_TESTES_PIXFLOW.pdf # Manual Oficial de Testes diagramado em PDF (13 páginas)
│   ├── MANUAL_DE_TESTES.md          # Manual de Testes e Homologação Técnica em Markdown
│   └── manual_testes_source.html    # Código-fonte HTML diagramado para geração do PDF
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/matheus/pixflow/
│   │   │   │   ├── PixFlowApplication.java
│   │   │   │   ├── domain/          # Entidades puras, Enums, Cpf/Cnpj Validators (Módulo 11)
│   │   │   │   ├── application/     # Casos de uso, DTOs, Batch Services e Webhooks
│   │   │   │   └── infrastructure/  # Adaptadores REST, Persistência JPA, SQS e Webhooks
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   │           ├── V1__init_schema.sql        # Esquema de charges e settlement_audit_logs
│   │   │           └── V2__webhook_deliveries.sql # Esquema de auditoria de webhooks
│   │   └── test/java/com/matheus/pixflow/         # 141 Testes Unitários e MockMvc (100% Cobertura)
│   ├── Dockerfile                   # Multi-Stage Build (JDK 21 compile -> JRE 21 Alpine runner)
│   ├── pom.xml                      # Dependências Maven e Plugin JaCoCo (100% Cobertura)
│   └── mvnw                         # Maven Wrapper oficial
├── frontend/
│   ├── src/
│   │   ├── views/
│   │   │   ├── TerminalView.jsx     # Emissão PIX com validação Módulo 11 e sample keys
│   │   │   ├── LedgerView.jsx       # Livro-Razão de alta densidade e exportação CSV
│   │   │   ├── StressLabView.jsx    # Gerador de concorrência e carga em lote
│   │   │   ├── WebhooksView.jsx     # Rastreamento de entregas, HMAC-SHA256 e teste de adulteração
│   │   │   └── TutorialView.jsx     # Roteiro interativo de testes com 6 botões 1-clique
│   │   ├── components/
│   │   │   ├── Navbar.jsx           # Navegação institucional fintech e telemetria
│   │   │   ├── StatusBadge.jsx      # Badges minimalistas de status
│   │   │   ├── TransactionDrawer.jsx# Gaveta lateral com Timeline do worker e JSON
│   │   │   ├── LatencyMonitor.jsx   # Indicadores de volume, sucesso e tempo de liquidação
│   │   │   ├── QrCodeModal.jsx      # Modal EMVCo BR Code
│   │   │   └── ArchitectureModal.jsx# Diagrama arquitetural
│   │   ├── utils/
│   │   │   └── validators.js        # Validadores canônicos Módulo 11 no frontend
│   │   ├── services/api.js          # Cliente HTTP unificado
│   │   ├── App.jsx                  # Coordenador de estado SPA com navegação por URLs Hash
│   │   └── main.jsx
│   ├── nginx.conf                   # Configuração Nginx de produção (SPA fallback, proxy reverso, gzip)
│   ├── Dockerfile                   # Multi-stage build Node -> Nginx Alpine
│   ├── package.json                 # React 18, Vite 5, Tailwind CSS, Lucide Icons, QRCode.react
│   ├── tailwind.config.js
│   └── vite.config.js
├── terraform/
│   ├── main.tf                      # VPC, Subnets, SG, EC2 t3.micro, SQS + DLQ, S3 e IAM
│   ├── variables.tf                 # Parametrização para AWS Cloud e LocalStack
│   ├── outputs.tf                   # URLs, ARNs e IPs de saída
│   └── terraform.tfvars.example
├── docker-compose.yml               # Orquestração local em 1 comando (App, Postgres, LocalStack)
└── README.md                        # Documentação Técnica Internacional
```

---

## 🔌 Tabela de Endpoints REST

A documentação interativa com Swagger UI está acessível em `/swagger-ui.html`.

| Método | Endpoint | Descrição | Status Sucesso | Status Erro |
| :--- | :--- | :--- | :---: | :---: |
| `POST` | `/api/v1/charges` | Emite uma nova cobrança PIX com QR Code EMVCo | `201 Created` | `400`, `422` |
| `POST` | `/api/v1/charges/batch` | Emite lote de cobranças concorrentes em paralelo | `200 OK` | `400` |
| `GET` | `/api/v1/charges` | Lista transações com paginação (`page`, `size`) | `200 OK` | `400` |
| `GET` | `/api/v1/charges/{id}` | Consulta detalhes de uma cobrança por UUID | `200 OK` | `404` |
| `GET` | `/api/v1/charges/{id}/timeline` | Consulta timeline completa de auditoria do worker | `200 OK` | `404` |
| `GET` | `/api/v1/charges/export` | Exporta todo o histórico do livro-razão em formato CSV | `200 OK` | `500` |
| `POST` | `/api/v1/charges/{id}/cancel` | Cancela uma cobrança em estado `PENDING` | `200 OK` | `404`, `409` |
| `POST` | `/api/v1/charges/{id}/settle` | Simula compensação bancária forçada imediata | `200 OK` | `404`, `409` |
| `GET` | `/api/v1/webhooks` | Lista histórico de webhooks disparados pelo motor | `200 OK` | `500` |
| `GET` | `/api/v1/webhooks/charges/{chargeId}` | Consulta webhooks emitidos para uma cobrança específica | `200 OK` | `404` |
| `POST` | `/api/v1/webhooks/{id}/replay` | Simula reenvio (replay) seguro de uma notificação | `200 OK` | `404` |
| `POST` | `/api/v1/webhooks/verify` | Valida autenticidade criptográfica de um payload HMAC-SHA256 | `200 OK` | `400` |
| `GET` | `/api/v1/metrics` | Retorna métricas de volume, latência e fila | `200 OK` | `500` |
| `GET` | `/actuator/health` | Healthcheck do Spring Boot Actuator | `200 OK` | `503` |

---

## 🧪 Manual de Testes & Homologação

> 📄 **Documentação Oficial em PDF:** O manual técnico completo para homologação com 13 páginas diagramadas e tabelas de validação está disponível para download em [**docs/MANUAL_DE_TESTES_PIXFLOW.pdf**](docs/MANUAL_DE_TESTES_PIXFLOW.pdf). Para a versão em markdown, veja [**docs/MANUAL_DE_TESTES.md**](docs/MANUAL_DE_TESTES.md).

---

### 1. Matriz de Massa de Dados & Validadores Canônicos (Módulo 11)

O motor valida rigorosamente identificadores no domínio. Dados com dígitos verificadores incorretos ou formados por números repetidos são rejeitados com `HTTP 422 Unprocessable Entity`:

| Tipo de Chave | Valor Canônico para Teste | Status Esperado | Regra Validada |
| :--- | :--- | :---: | :--- |
| **CPF (Válido)** | `52998224725` (ou `529.982.247-25`) | `VÁLIDO` | Dígitos verificadores Módulo 11 da Receita Federal. |
| **CPF (Inválido)** | `12345678911` | `REJEITADO (422)` | Dígito verificador matemático incorreto. |
| **CPF (Repetido)** | `11111111111` | `REJEITADO (422)` | Sequência proibida de 11 dígitos repetidos. |
| **CNPJ (Válido)** | `04252011000110` (ou formatado) | `VÁLIDO` | CNPJ com pesos 2 a 9 e cálculo Módulo 11. |
| **CNPJ (Inválido)**| `00000000000199` | `REJEITADO (422)` | Dígitos verificadores de controle incorretos. |
| **E-mail (Válido)**| `devops@pixflow.io` | `VÁLIDO` | Sintaxe em conformidade com a RFC 5322. |
| **Telefone** | `+5511999998888` | `VÁLIDO` | Padrão internacional ITU-T E.164 (+55). |
| **Chave EVP** | `a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d` | `VÁLIDO` | Chave aleatória formato UUID v4 (36 chars). |

---

### 2. Roteiro de Homologação na Interface Web (SPA Vercel)

A interface SPA minimalista permite navegar diretamente com sincronização de URLs hash:

1. **Terminal PIX ([#terminal](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#terminal))**:
   - Clique em qualquer atalho de chave rápida (ex: `529.982.247-25`), defina o valor (ex: `R$ 150,00`) e clique em **"Emitir Cobrança PIX"**.
   - Valide o `EndToEndId` gerado (32 caracteres), o payload do BR Code e clique em **"Ver QR Code"** para abrir o modal gráfico.
2. **Livro-Razão & Gaveta de Auditoria ([#ledger](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#ledger))**:
   - Clique em qualquer linha para abrir a gaveta lateral (**Transaction Drawer**).
   - Inspecione a **Linha do Tempo (Timeline do Worker)**: acompanhe a latência de cada transição (`CREATED` ➔ `SQS_ENQUEUED` ➔ `SETTLEMENT_STARTED` ➔ `SETTLED`).
   - Clique em **"Exportar Livro-Razão (CSV)"** para obter o extrato completo em planilha.
3. **Stress Lab ([#stress](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#stress))**:
   - Selecione um lote de 5, 10 ou 20 transações simultâneas e clique em **"Disparar Lote de Concorrência"**.
   - Analise o Throughput calculado (**TPS**), a latência agregada em ms e o status das cobranças processadas em threads paralelas.
4. **Auditoria de Webhooks & Verificador Criptográfico ([#webhooks](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#webhooks))**:
   - Inspecione as notificações enviadas e o cabeçalho `X-Signature-SHA256`.
   - Clique em **"Replay Webhook"** para re-despachar a notificação com nova assinatura e timestamp.
   - No laboratório inferior, clique em **"Verificar com API Backend"** (`Assinatura VÁLIDA`) e depois em **"Simular Fraude / Adulteração"** (`Assinatura INVÁLIDA`), comprovando a detecção de fraude por HMAC.
5. **Roteiro Interativo com 6 Testes em 1-Clique ([#tutorial](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#tutorial))**:
   - Execute sequencialmente os 6 testes pré-programados para validar toda a plataforma sem digitar nada.

---

### 3. Roteiro de Testes via Terminal & cURL

#### Cenário 1: Emissão Happy Path (CPF Válido)
```bash
curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/charges \
  -H "Content-Type: application/json" \
  -d '{
    "pixKey": "52998224725",
    "pixKeyType": "CPF",
    "amount": 150.00,
    "description": "Liquidação Happy Path",
    "webhookUrl": "https://webhook.site/audit-log"
  }' | jq .
```

#### Cenário 2: Validação Canônica Módulo 11 (Rejeição de CPF Inválido)
```bash
curl -s -i -X POST https://pix-flow-backend.onrender.com/api/v1/charges \
  -H "Content-Type: application/json" \
  -d '{
    "pixKey": "12345678911",
    "pixKeyType": "CPF",
    "amount": 50.00
  }'
# Retorna: HTTP/2 422 Unprocessable Entity com mensagem descritiva do Módulo 11
```

#### Cenário 3: Cancelamento de Cobrança Pré-Liquidação
```bash
# 1. Cria cobrança
ID=$(curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/charges \
  -H "Content-Type: application/json" \
  -d '{"pixKey":"admin@pixflow.io","pixKeyType":"EMAIL","amount":25.00}' | jq -r .id)

# 2. Cancela imediatamente
curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/charges/$ID/cancel | jq '{id, status}'
```

#### Cenário 4: Inspeção da Linha do Tempo (Timeline do Worker)
```bash
curl -s https://pix-flow-backend.onrender.com/api/v1/charges/$ID/timeline | jq .
```

#### Cenário 5: Teste de Carga e Concorrência em Lote (Batch)
```bash
curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/charges/batch \
  -H "Content-Type: application/json" \
  -d '{
    "charges": [
      {"pixKey": "52998224725", "pixKeyType": "CPF", "amount": 10.00},
      {"pixKey": "04252011000110", "pixKeyType": "CNPJ", "amount": 20.00},
      {"pixKey": "tech@pixflow.io", "pixKeyType": "EMAIL", "amount": 30.00}
    ]
  }' | jq .
```

#### Cenário 6: Verificação de Integridade Criptográfica HMAC-SHA256
```bash
curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/webhooks/verify \
  -H "Content-Type: application/json" \
  -d '{
    "payload": "{\"chargeId\":\"e4c83a12-8610-41bf-a849-0129bb8e2efb\",\"status\":\"SETTLED\",\"amount\":150.00}",
    "signature": "ba6c72e41f059c2567dfb642ec30fa8e6a578a1098e945c57b85041ff34e2f81"
  }' | jq .
```

---

### 4. Testes Locais com Docker Compose & LocalStack
```bash
# 1. Subir backend, banco PostgreSQL e LocalStack AWS SQS
docker compose up -d --build

# 2. Listar filas SQS no LocalStack
docker exec -it pixflow-localstack awslocal sqs list-queues

# 3. Consumir mensagem da fila
docker exec -it pixflow-localstack awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/pix-settlement-queue

# 4. Conectar diretamente ao banco PostgreSQL
docker exec -it pixflow-postgres psql -U pixflow -d pixflow -c "SELECT id, status, amount FROM charges LIMIT 5;"
```

---

### 5. Execução dos Testes Automatizados no Backend (100% JaCoCo)
```bash
cd backend
chmod +x mvnw
./mvnw clean verify -B
```
- **Total:** 141 testes automatizados (0 falhas, 0 erros, 0 pulados).
- **Cobertura:** 100.00% de instruções (3.427 / 3.427) e 100.00% de branches (173 / 173). Relatório gerado em `backend/target/site/jacoco/index.html`.

---

## ☁️ Guia de Deploy 100% Gratuito

```mermaid
graph LR
    Neon["1. Neon.tech<br/>PostgreSQL Serverless<br/>(Grátis 0.5GB)"]
    Render["2. Render.com<br/>Backend Docker Web Service<br/>(Plano Free)"]
    Vercel["3. Vercel.com<br/>Frontend React SPA<br/>(Plano Hobby Free)"]

    Neon -->|Connection Pool String| Render
    Render -->|HTTPS REST API / CORS| Vercel
```

### Passo 1: Banco de Dados PostgreSQL no Neon (neon.tech)
1. Acesse [neon.tech](https://neon.tech) e crie uma conta gratuita.
2. Crie um projeto chamado `pixflow-db` na região `sa-east-1` ou `us-east-1`.
3. Obtenha a connection string JDBC com SSL habilitado:
   ```text
   jdbc:postgresql://<host-neon>/neondb?sslmode=require
   ```
*(O Flyway aplicará automaticamente as migrations `V1__init_schema.sql` e `V2__webhook_deliveries.sql` na inicialização).*

---

### Passo 2: Core Backend no Render (render.com)
1. Acesse [render.com](https://render.com) e conecte o repositório `matheus-araujo-pereira/pix-flow`.
2. Configure o serviço:
   - **Root Directory**: `backend`
   - **Runtime**: `Docker`
   - **Instance Type**: `Free`
3. Configure as variáveis de ambiente:
   | Chave | Valor |
   | :--- | :--- |
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_URL` | URL JDBC do Neon (`?sslmode=require`) |
   | `DB_USER` | Usuário do Neon |
   | `DB_PASSWORD` | Senha do Neon |
   | `PIXFLOW_MESSAGING_PROVIDER` | `in-memory` |
   | `PORT` | `8080` |
   | `CORS_ALLOWED_ORIGINS` | `https://*.vercel.app,http://localhost:5173` |

---

### Passo 3: Frontend SPA na Vercel (vercel.com)
1. Acesse [vercel.com](https://vercel.com) e importe o repositório.
2. Defina o **Root Directory** como `frontend`.
3. Adicione a variável de ambiente:
   - `VITE_API_URL`: `https://pix-flow-backend.onrender.com`
4. Clique em **Deploy**. O painel com design minimalista estará online imediatamente!

---

### Passo 4: Execução Local no Fedora 44 (KDE Plasma)

```bash
# 1. Instalar dependências no Fedora 44
sudo dnf install -y git podman podman-compose java-21-openjdk-devel nodejs

# 2. Subir infraestrutura local via Podman
podman-compose up -d --build

# 3. Rodar Frontend em modo hot-reload
cd frontend
npm install
npm run dev
# Acesse: http://localhost:5173
```

---

## 🧪 Engenharia de Qualidade & Testes

Para executar toda a suíte de testes unitários e de integração validando 100% de cobertura com o JaCoCo:

```bash
cd backend
./mvnw clean verify
```

O relatório HTML detalhado de cobertura por pacote e branch é gerado em:
```text
backend/target/site/jacoco/index.html
```

---

## 👨‍💻 Autor

**Matheus Araujo Pereira**  
- GitHub: [@matheus-araujo-pereira](https://github.com/matheus-araujo-pereira)  
- LinkedIn: [Matheus Araujo Pereira](https://linkedin.com)  
- Email: matharaupere@gmail.com  

---

<div align="center">
  <sub>Desenvolvido com foco em excelência arquitetural, portabilidade e resiliência transacional sob Java 21, AWS e Linux.</sub>
</div>
