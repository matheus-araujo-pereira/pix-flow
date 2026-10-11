# 🧪 PixFlow: Manual Oficial de Testes & Homologação Técnica

> **Versão do Motor:** 1.0.0-PROD | **Ambiente de Testes:** 100% Gratuito (Render, Neon, Vercel, LocalStack)  
> **PDF Oficial para Download:** [📄 Baixar MANUAL_DE_TESTES_PIXFLOW.pdf](./MANUAL_DE_TESTES_PIXFLOW.pdf)

---

## 📌 1. Visão Geral dos Testes & Topologia do Sistema

O **PixFlow** é um motor de mensageria e liquidação transacional assíncrona para arranjos de pagamentos instantâneos (padrão BACEN / SPI). Este documento fornece o roteiro passo a passo para testar, validar e homologar todas as funcionalidades do projeto tanto pela **Interface Web (React SPA na Vercel)** quanto via **Terminal / Linha de Comando (cURL)** e **Testes Automatizados (JUnit 5 + JaCoCo)**.

### Links Diretos para Testes Online
- 🚀 **Interface Web Homologada na Vercel:** [https://frontend-hazel-six-05z2k7m6sk.vercel.app](https://frontend-hazel-six-05z2k7m6sk.vercel.app)
- 📖 **Swagger / OpenAPI Interativo:** [https://pix-flow-backend.onrender.com/swagger-ui.html](https://pix-flow-backend.onrender.com/swagger-ui.html)
- 🩺 **Healthcheck Spring Actuator:** `https://pix-flow-backend.onrender.com/actuator/health`

---

## 📊 2. Matriz de Massa de Dados & Validadores Canônicos (Módulo 11)

O PixFlow implementa validação matemática estrita no nível de domínio para chaves PIX. Chaves com dígitos verificadores incorretos ou formadas por números repetidos são rejeitadas com `HTTP 422 Unprocessable Entity`.

| Tipo de Chave | Valor de Teste Recomendado | Status Esperado | Descrição da Regra Validada |
| :--- | :--- | :---: | :--- |
| **CPF (Válido)** | `52998224725` (ou formatado) | `VÁLIDO` | Dígitos verificadores validados pela fórmula Módulo 11 da Receita Federal. |
| **CPF (Inválido)** | `12345678911` | `REJEITADO (422)` | Dígito verificador matemático incorreto. |
| **CPF (Sequência)** | `11111111111` | `REJEITADO (422)` | Sequência proibida de 11 dígitos repetidos. |
| **CNPJ (Válido)** | `04252011000110` (ou formatado) | `VÁLIDO` | CNPJ com pesos 2 a 9 e cálculo Módulo 11 rigoroso. |
| **CNPJ (Inválido)** | `00000000000199` | `REJEITADO (422)` | Dígito verificador empresarial incorreto. |
| **EMAIL (Válido)** | `devops@pixflow.io` | `VÁLIDO` | Conforme especificação RFC 5322. |
| **PHONE (Válido)** | `+5511999998888` | `VÁLIDO` | Formato internacional ITU-T E.164 com código de país (+55). |
| **RANDOM (EVP)** | `a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d` | `VÁLIDO` | Chave aleatória formato UUID v4 (36 caracteres). |

---

## 🖥️ 3. Roteiro Passo a Passo na Interface Web (SPA Vercel)

A interface SPA permite navegar diretamente entre as telas com sincronização de URLs hash:

### 3.1. Tela 1: Terminal de Emissão PIX (`#terminal`)
1. Acesse o [Terminal PIX](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#terminal).
2. Clique no atalho de chave rápida (ex: `529.982.247-25`).
3. Preencha o valor (ex: `R$ 150,00`) e a descrição.
4. Clique em **"Emitir Cobrança PIX"**.
5. **Resultado:**
   - Card verde de confirmação com `EndToEndId` padrão BACEN (32 caracteres).
   - Payload do Pix Copia e Cola (Tags TLV 00 a 63 com checksum CRC-16).
   - Botão **"Ver QR Code"** que abre o modal gráfico do QR Code renderizado.

### 3.2. Tela 2: Livro-Razão & Gaveta Lateral de Auditoria (`#ledger`)
1. Acesse o [Livro-Razão](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#ledger).
2. Veja a listagem de todas as transações cadastradas ordenadas por data.
3. Clique em qualquer linha para abrir a **Gaveta Lateral de Inspeção (Transaction Drawer)**.
4. Na gaveta, examine a **Linha do Tempo (Timeline do Worker)**:
   - `CREATED` ➔ `SQS_ENQUEUED` ➔ `SETTLEMENT_STARTED` ➔ `SETTLED`.
   - Latência medida em milissegundos para cada transição.
5. Clique em **"Exportar Livro-Razão (CSV)"** para baixar a planilha completa em formato `.csv`.

### 3.3. Tela 3: Laboratório de Carga & Concorrência (`#stress`)
1. Acesse o [Stress Lab](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#stress).
2. Escolha o tamanho do lote: 5, 10 ou 20 transações simultâneas.
3. Clique em **"Disparar Lote de Concorrência"**.
4. **Resultado:**
   - O backend Spring Boot processa o lote com threads paralelas.
   - O painel exibe o **Throughput (TPS)**, a latência total e o resumo do lote.

### 3.4. Tela 4: Auditoria de Webhooks & Verificador Criptográfico (`#webhooks`)
1. Acesse [Webhooks & HMAC](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#webhooks).
2. Clique em qualquer evento de webhook registrado na coluna esquerda.
3. Observe o cabeçalho `X-Signature-SHA256` e o JSON enviado no corpo da requisição.
4. Clique em **"Replay Webhook"**: o motor re-despacha a notificação com novo timestamp e assinatura segura.
5. No painel inferior **"Laboratório de Verificação Criptográfica"**:
   - Clique em **"Verificar com API Backend"** ➔ Retorna: `Assinatura VÁLIDA (Autêntica)`.
   - Clique em **"Simular Fraude / Adulteração"** (que altera o payload) e clique novamente em verificar ➔ Retorna: `Assinatura INVÁLIDA (Adulterada)`, provando a proteção contra adulteração de dados.

### 3.5. Tela 5: Roteiro Interativo com 6 Testes em 1-Clique (`#tutorial`)
1. Acesse [Roteiro & Testes](https://frontend-hazel-six-05z2k7m6sk.vercel.app/#tutorial).
2. Execute cada teste pré-programado:
   - **Teste 1:** Emissão Happy Path (CPF Válido)
   - **Teste 2:** Rejeição de CPF Inválido (Módulo 11)
   - **Teste 3:** Emissão Empresarial com CNPJ
   - **Teste 4:** Emissão e Cancelamento Imediato (Rollback)
   - **Teste 5:** Carga Concorrente de 5 Transações
   - **Teste 6:** Auditoria de Webhook e Assinatura Criptográfica

---

## ⚡ 4. Roteiro Passo a Passo via Terminal & cURL

### 4.1. Emissão de Cobrança Instantânea
```bash
curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/charges \
  -H "Content-Type: application/json" \
  -d '{
    "pixKey": "52998224725",
    "pixKeyType": "CPF",
    "amount": 150.00,
    "description": "Serviço Cloud DevOps",
    "webhookUrl": "https://webhook.site/audit-log"
  }' | jq .
```

### 4.2. Teste de Rejeição Módulo 11 (HTTP 422)
```bash
curl -s -i -X POST https://pix-flow-backend.onrender.com/api/v1/charges \
  -H "Content-Type: application/json" \
  -d '{
    "pixKey": "12345678911",
    "pixKeyType": "CPF",
    "amount": 50.00
  }'
# Resposta esperada: HTTP/2 422 Unprocessable Entity
```

### 4.3. Cancelamento Pré-Liquidação
```bash
# 1. Cria cobrança
ID=$(curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/charges \
  -H "Content-Type: application/json" \
  -d '{"pixKey":"admin@pixflow.io","pixKeyType":"EMAIL","amount":25.00}' | jq -r .id)

# 2. Cancela antes da compensação
curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/charges/$ID/cancel | jq '{id, status}'
```

### 4.4. Consulta da Timeline de Auditoria
```bash
curl -s https://pix-flow-backend.onrender.com/api/v1/charges/$ID/timeline | jq .
```

### 4.5. Disparo em Lote Concorrente
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

### 4.6. Exportação do Livro-Razão em CSV
```bash
curl -s -O https://pix-flow-backend.onrender.com/api/v1/charges/export
head -n 5 export
```

### 4.7. Re-despacho Seguro de Webhook (Replay)
```bash
WEBHOOK_ID=$(curl -s "https://pix-flow-backend.onrender.com/api/v1/webhooks?limit=1" | jq -r '.[0].id')
curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/webhooks/$WEBHOOK_ID/replay | jq .
```

### 4.8. Validação Criptográfica de Assinatura HMAC-SHA256
```bash
curl -s -X POST https://pix-flow-backend.onrender.com/api/v1/webhooks/verify \
  -H "Content-Type: application/json" \
  -d '{
    "payload": "{\"chargeId\":\"e4c83a12-8610-41bf-a849-0129bb8e2efb\",\"status\":\"SETTLED\",\"amount\":150.00}",
    "signature": "ba6c72e41f059c2567dfb642ec30fa8e6a578a1098e945c57b85041ff34e2f81"
  }' | jq .
```

---

## 🐳 5. Testes no Ambiente Local com Docker Compose & LocalStack

Para executar toda a infraestrutura em ambiente isolado:
```bash
# Subir aplicação, banco PostgreSQL e LocalStack AWS SQS
docker compose up -d --build

# Verificar saúde dos containers
docker compose ps

# Listar filas SQS no LocalStack
docker exec -it pixflow-localstack awslocal sqs list-queues

# Consumir mensagens da fila de liquidação
docker exec -it pixflow-localstack awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/pix-settlement-queue

# Acessar dados no PostgreSQL local
docker exec -it pixflow-postgres psql -U pixflow -d pixflow -c "SELECT id, status, amount FROM charges LIMIT 5;"
```

---

## 🎯 6. Suíte de Testes Automatizados no Backend (100% JaCoCo)

Para executar a suíte completa de **141 testes automatizados** e verificar a cobertura de código:

```bash
cd backend
chmod +x mvnw
./mvnw clean verify -B
```

### Métricas Auditadas de Cobertura
- **Testes Executados:** 141 (0 falhas, 0 erros, 0 pulados)
- **Instruções de Bytecode (INSTRUCTION):** 3.427 / 3.427 (**100.00%**)
- **Ramificações Lógicas (BRANCH):** 173 / 173 (**100.00%**)
- **Linhas de Código (LINE):** 782 / 782 (**100.00%**)
- **Relatório HTML JaCoCo:** `backend/target/site/jacoco/index.html`
