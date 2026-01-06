# Projeto Hanami Backend — API de Análise de Dados

API backend desenvolvida em **Java com Spring Boot** para processamento de arquivos **CSV/XLSX** e geração de **relatórios analíticos de vendas**.  
Este projeto faz parte do **Projeto Hanami**, uma iniciativa de impacto social voltada ao uso de tecnologia para análise de dados.

---

## 📌 Visão Geral do Projeto

- **Prazo total:** 40 dias
- **Metodologia:** Desenvolvimento incremental por sprints
- **Sprint atual:** Sprint 1 – Fundação e Setup do Projeto
- **Status atual:** Finalizada

### 🎯 Objetivo Geral
Desenvolver uma API robusta capaz de:
- Receber arquivos CSV/XLSX
- Processar dados de vendas
- Armazenar informações em banco de dados
- Gerar relatórios analíticos

---

## 🗂️ Planejamento por Sprints

### Sprint 1 — Fundação e Início do Desenvolvimento
| Foco Principal | Entregas |
|---------------|---------|
| Setup do projeto e arquitetura base | Estrutura inicial do projeto |
| Configuração de ambiente | Perfis `dev` e `prod` |
| Início do backend | Parser de dados e endpoint de upload |
| Persistência | Entidades e repositórios iniciais |

### Sprint 2 — Consolidação e Deploy *(planejada)*
| Foco Principal | Entregas |
|---------------|---------|
| Finalização da lógica de análise | Algoritmos completos |
| Relatórios | Geração de relatórios PDF |
| Documentação | README final e instruções de uso |
| Deploy | Ambiente produtivo |

> 🔎 **Observação:** A Sprint 2 será detalhada após a conclusão da Sprint 1.

---

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot**
- **MySQL**
- **Maven**

---

## 📁 Estrutura do Projeto

```text
src/main/java/com/hanami/iurydev/apiHanami
├── controller     # Camada de controle (endpoints REST)
├── dto            # Objetos de transferência de dados
├── entity
│   ├── embeddable # Objetos incorporáveis
│   ├── enums      # Enumerações do domínio
│   └── Venda      # Entidade principal de vendas
├── repository     # Interfaces JPA
├── service        # Regras de negócio
└── ApiHanamiApplication
```
---

## Configuração para ambiente de desenvolvimento e produção
### Vá em resources e crie um arquivo application-dev.properties adicione:
```
    spring.datasource.url=jdbc:mysql://localhost:3306/hanamiapidb
    spring.datasource.username=seu-login-mysql
    spring.datasource.password=sua-senha-mysql
    
    spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.jpa.show-sql=true
    spring.jpa.hibernate.ddl-auto=update
    
    # Aumenta o limite de tamanho do arquivo individual
    spring.servlet.multipart.max-file-size=50MB
    
    # Aumenta o limite total da requisição (arquivo + dados extras)
    spring.servlet.multipart.max-request-size=50MB
    
    spring.jackson.date-format=yyyy-MM-dd
    spring.jackson.time-zone=America/Sao_Paulo
``` 

### Na mesma pasta crie um arquivo application-prod.properties(para ambiente de produção) adicione:
``` 
    spring.datasource.url=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}
    spring.datasource.username=${MYSQLUSER}
    spring.datasource.password=${MYSQLPASSWORD}
    
    spring.jpa.hibernate.ddl-auto=update
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.jpa.show-sql=false
    spring.jpa.properties.hibernate.format_sql=true
    
    spring.jackson.date-format=yyyy-MM-dd
    spring.jackson.time-zone=America/Sao_Paulo
```

### No application.properties adicione:
```
    spring.profiles.active=dev
```

---

### ▶️ Como Rodar o Projeto
#### Pré-requisitos
- Java 17
- MySQL

**Passo a passo**
1. Clone o repositório
```bash
   git clone https://github.com/IuryDevJava/api-hanami.git
```

2. Entre no diretório
```bash
   cd api-hanami
```

3. Execute a aplicação
```bash
   ./mvnw spring-boot:run
```

---

### Veja se as dependências necessárias para a leitura de arquivos estão no arquivo pom.xml
```xml
   <!-- Leitura de arquivos XLSX -->
    <dependency>
      <groupId>org.apache.poi</groupId>
      <artifactId>poi-ooxml</artifactId>
      <version>5.2.3</version>
    </dependency>
    
    <!-- Leitura de arquivos CSV -->
    <dependency>
      <groupId>com.opencsv</groupId>
      <artifactId>opencsv</artifactId>
      <version>5.5.2</version>
    </dependency>
```

---

### Endpoints REST - Sprint 1
#### Upload de Arquivo de Vendas
**Endpoint responsável por receber arquivos CSV ou XLSX, validar os dados, processar as vendas e persistir no banco de dados**

---

*Post /vendas/upload*
#### Descrição
#### Realiza o upload de um arquivo CSV ou XLSX contendo dados de vendas

- **Valida a estrutura do arquivo**
- **Valida regras de negócio campo a campo**
- **Evita duplicidade por id_transacao**
- **Persiste apenas registros válidos**
- **Marca registros inválidos com observações**

---

#### Requisição

- **URL: /vendas/upload**
- **Método: POST**
- **Content-Type: multipart/form-data**

#### Parâmetro (Body)
| Nome                     | Tipo | descrição | 
|--------------------------|------|-----------|
| file                     | File | Arquivo CSV ou XLSX com os dados de vendas |

---

#### Chamada (Postman)

**Body**
- **Type: form-data**
- **Key: file**
- **Type: File**
- **Value: vendas_ficticias_10000_linhas.csv**

---

### Respostas da API - métodos POST
#### ✅ 200 OK - Upload feito com sucesso
##### Retornado quando o arquivo é processado de forma correta e contêm registros validados
```json
   {
  "status": "sucesso",
  "linhas_processadas": 10000
   }
```

---

#### ⚠️ 200 OK — Nenhuma nova linha processada
##### Retornado quando o arquivo é válido, mas não há novas vendas para persistir (ex: dados duplicados)
```json
   {
  "status": "Aviso: Nenhuma nova linha processada",
  "linhas_processadas": 0
   }
```

---

#### 400 Bad Request — Arquivo não enviado
##### Retornado quando o parâmetro file não é enviado ou está vazio
```json
   {
  "status": "erro",
  "linhas_processadas": 0
   }
```

---

#### 422 Unprocessable Entity - Estrutura inválida
##### Retornado quando o arquivo não possui colunas obrigatórias.
```json
   {
  "status": "Coluna obrigatória ausente: id_transacao",
  "linhas_processadas": 0
   }
```

---

### Métodos GET 
##### {{base_url}}/vendas/reports/sales-summary - Retorna total de vendas e a média por transação.
```json
   {
  "receita_liquida": 5243176617.89,
  "lucro_bruto": 3099751358.11,
  "total_vendas": 8342927976.00,
  "media_por_transacao": 928849.70,
  "custo_total": 5243176617.89,
  "numero_transacoes": 8982
   }
```

---

##### {{base_url}}/vendas/reports/product-analysis - Retorna uma lista de produtos sem ordenação.
```json
   [
  {
    "nome_produto": "Carregador Wireless",
    "quatidade_vendida": 1006,
    "total_arrecadado": 288235871.00
  },
  {
    "nome_produto": "iPhone 15",
    "quatidade_vendida": 787,
    "total_arrecadado": 246201201.00
  },
  {
    "nome_produto": "Apple Watch",
    "quatidade_vendida": 858,
    "total_arrecadado": 249837283.00
  }
  ]
```

---

##### {{base_url}}/vendas/reports/product-analysis?sort_by=quantidade - Retorna os produtos de forma ordenada e por quantidade.
```json
   [
  {
    "nome_produto": "Cabo USB-C",
    "quatidade_vendida": 1061,
    "total_arrecadado": 339386041.00
  },
  {
    "nome_produto": "Webcam HD",
    "quatidade_vendida": 1026,
    "total_arrecadado": 319378902.00
  },
  {
    "nome_produto": "Carregador Wireless",
    "quatidade_vendida": 1006,
    "total_arrecadado": 288235871.00
  }
  ]
```

---

##### {{base_url}}/vendas/reports/product-analysis?sort_by=valor - Retorna os produtos de forma ordenada e por valor.
```json
   [
  {
    "nome_produto": "Cabo USB-C",
    "quatidade_vendida": 1061,
    "total_arrecadado": 339386041.00
  },
  {
    "nome_produto": "Webcam HD",
    "quatidade_vendida": 1026,
    "total_arrecadado": 319378902.00
  },
  {
    "nome_produto": "Chromecast",
    "quatidade_vendida": 934,
    "total_arrecadado": 294529780.00
  }
  ]
```

---

##### {{base_url}}/vendas/reports/financial-metrics - Retorna um JSON com lucro_bruto, receita_liquida e custo_total.
```json
   {
  "receita_liquida": 5243176617.89,
  "lucro_bruto": 3099751358.11,
  "custo_total": 5243176617.89
   }
```

---

### Regras de Validação Aplicadas
#### Durante o processamento do arquivo, são aplicadas validações como:

- **Formato do id_transacao (ex: TXN12345678)**
- **Margem de lucro mínima e máxima**
- **Idade do cliente**
- **Formato de IDs de cliente, produto e vendedor**
- **Datas válidas**
- **Campos obrigatórios não nulos**
- **Enumerações normalizadas (canal de venda, forma de pagamento, região, status de entrega)**

---

### Banco de Dados MySQL
#### Modelo de Dados
#### A aplicação utiliza o modelo de persistência onde os dados do Produto são tratados como objetos incorporáveis (@Embeddable), resultando em uma tabela única de vendas para otimização de performance analítica.
#### Criar e usar o banco (não esqueça que o nome do banco precisa ser o mesmo no arquivo properties em spring.datasource.url=jdbc:mysql://localhost:3306/hanamiapidb)
```sql
   CREATE DATABASE hanamiapidb;
   USE hanamiapidb;
```

#### Listar tabelas
```sql
   SHOW TABLES;
```

#### Mostra o total de registros
```sql
   SELECT COUNT(*) FROM vendas;
```

#### Mostra em tabelas com dados os 10 primeiros registros
```sql
   SELECT * FROM vendas LIMIT 10;
```

#### Registros inválidos
```sql
   SELECT id_transacao, observacao_validada
   FROM vendas
   WHERE processado_sucesso = false;
```

#### Estatística de processamento
```sql
   SELECT processado_sucesso, COUNT(*)
   FROM vendas
   GROUP BY processado_sucesso;
```

#### Limpar tabela
```sql
   DROP TABLE hanamiapidb.vendas;
```

#### Valida o endpoint que retorna os 6 campos. Faz a soma total, calcula a média e conta as transações
```sql
   SELECT
       SUM(valor_final) AS total_vendas,
       SUM(valor_final * (margem_lucro / 100)) AS lucro_bruto,
       SUM(valor_final) - SUM(valor_final * (margem_lucro / 100)) AS receita_liquida,
       SUM(valor_final - (valor_final * (margem_lucro / 100))) AS custo_total,
       AVG(valor_final) AS media_por_transacao,
       COUNT(*) AS numero_transacoes
   FROM vendas
   WHERE processado_sucesso = 1;
```

#### Valida a lista de produtos, a quantidade vendida e o total arrecadado em ordem decrescente
```sql
   SELECT
       nome_produto,
       COUNT(*) AS quantidade_vendida,
       SUM(valor_final) AS total_arrecadado
   FROM vendas
   WHERE processado_sucesso = 1
   GROUP BY nome_produto
   ORDER BY total_arrecadado DESC;
```

#### Retorna lucro_bruto, receita_liquida e custo_total
```sql
   SELECT
       SUM(valor_final * (margem_lucro / 100)) AS lucro_bruto,
       SUM(valor_final) - SUM(valor_final * (margem_lucro / 100)) AS receita_liquida,
       SUM(valor_final - (valor_final * (margem_lucro / 100))) AS custo_total
   FROM vendas
   WHERE processado_sucesso = 1;
```

---

### Logs e Observabilidade
#### Visão Geral
#### A API utiliza logging estruturado para registrar eventos importantes durante o processamento de arquivos, facilitando:

- **Monitoramento da aplicação**
- **Debug de erros**
- **Auditoria de processamento**
- **Análise de falhas em produção**

#### Insira em cima da classe controller a seguinte anotação:
```java
   @Slf4j
   @RestController
   @RequestMapping("/vendas")
   public class VendaController {
   }
```

---

#### Logs de Sucesso
```text
   200 OK. Arquivo 'vendas_ficticias_10000_linhas.csv' foi processado com sucesso. Total: 10000 linhas
```

#### Quando ocorre?
- **Upload válido**
- **Arquivo lido corretamente**
- **Processamento finalizado sem erros**

---

#### Logs de Erro - Requisição Inválida (400)
```text
   Erro 400. Ao tentar fazer o upload sem arquivo foi retornado um erro
```

#### Quando ocorre?
- **Parâmetro file não enviado**
- **Arquivo vazio**

---

#### Logs de Erro — Estrutura de Arquivo Inválida (422)
```text
   Erro 422. Arquivo enviado não contém uma ou mais colunas obrigatórias Coluna obrigatória ausente: id_transacao
```

#### Quando ocorre?
- **CSV/XLSX não possui colunas obrigatórias**
- **Estrutura incompatível com o parser**

---

#### Logs de Erro Crítico — Falha Interna (500)
```text
   Erro crítico durante o processamento de upload
```

#### Quando ocorre?
- **Exceções inesperadas**
- **Falhas de I/O, parsing ou banco de dados**

---

### Check-list Final de Fechamento da Sprint 1:
**[X] Código: O projeto compila sem erros? (Sim)**
**[X] Testes: Os endpoints no Postman batem com os resultados do SQL? (Sim)**
**[X] Documentação: O README reflete a realidade do código? (Sim)**