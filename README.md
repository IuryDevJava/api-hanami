# Projeto Hanami Backend — API de Análise de Dados

API backend desenvolvida em **Java com Spring Boot** para processamento de arquivos **CSV/XLSX** e geração de **relatórios analíticos de vendas**.  
Este projeto faz parte do **Projeto Hanami**, uma iniciativa de impacto social voltada ao uso de tecnologia para análise de dados.

---

## 📌 Visão Geral do Projeto

- **Prazo total:** 40 dias
- **Metodologia:** Desenvolvimento incremental por sprints
- **Sprint atual:** Sprint 1 – Fundação e Setup do Projeto

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

### Respostas da API
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

#### 422 Unprocessable Entity — Estrutura inválida
##### Retornado quando o arquivo não possui colunas obrigatórias.
```json
   {
  "status": "Coluna obrigatória ausente: id_transacao",
  "linhas_processadas": 0
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