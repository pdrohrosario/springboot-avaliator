# Avaliator

### Arquitetura de Microserviços — Produtos, Avaliações e Métricas

## Descrição Geral
Este projeto é composto por três microserviços principais, integrados por meio de eventos Kafka para oferecer um ecossistema robusto de gerenciamento de produtos, avaliações e métricas agregadas.

## 1. Microserviço de Produtos
   📌 **Responsabilidade**
   - Gerenciar produtos e suas informações principais.

   🛠️ **Funções**
   - **CRUD de Produtos**: Cadastro, atualização, consulta e exclusão de produtos.
   - **Informações do Produto**: Nome, descrição, categoria, preço, entre outros atributos.
   - **Métricas Agregadas**: Recebe métricas calculadas pelo microserviço de métricas e disponibiliza para o frontend.
   - **Publicação de Eventos**: Envia eventos Kafka (ex.: produto criado, produto atualizado).

   🗂️ **Entidades**
   - **Product**:
     - `id`: Identificador único do produto.
     - `name`: Nome do produto.
     - `description`: Descrição do produto.
     - `category`: Categoria do produto.
     - `price`: Preço do produto.
     - `metrics` (opcional): Métricas preenchidas pelo serviço de métricas.

   🔗 **Endpoints**
   - `POST /products`: Criar um produto.
   - `GET /products/{id}`: Consultar um produto.
   - `PUT /products/{id}`: Atualizar um produto.

   🧪 **Tecnologias**
   - Spring Boot.
   - Banco PostgreSQL.

## 2. Microserviço de Feedbacks (Avaliações)
   📌 **Responsabilidade**
   - Gerenciar todas as avaliações dos produtos.

   🛠️ **Funções**
   - **CRUD de Avaliações**: Criação, consulta e remoção de avaliações dos usuários.
   - **Publicação no Kafka**: Cada avaliação criada é dispara a públicação de um tópico para atualização das métricas de um produto.
   - **Informações de um feedback**:
     - Nota entre 1 e 5.
     - Comentário obrigatório (opcional conforme regra de negócio).
   - **Persistência**: Armazena avaliações em banco relacional ou NoSQL.

   🔗 **Endpoints**
   - `POST /reviews`: Criar uma avaliação.
   - `GET /reviews/product/{productId}`: Listar avaliações por produto.
   - `GET /reviews/{id}`: Consultar uma avaliação específica.
   - `GET /reviews/`: Listar todas as avaliação.

   🧪 **Tecnologias**
   - Spring Boot.
   - Kafka.
   - Banco PostgreSQL.

## 3. Microserviço de Cálculos e Métricas
   📌 **Responsabilidade**
   - Processar avaliações e gerar métricas agregadas.

   🛠️ **Funções**
   - **Consumir Avaliações via Kafka**: Cada nova avaliação é processada em tempo real.
   - **Cálculo de Métricas**:
     - Média de notas.
     - Total de avaliações.
     - Distribuição por estrelas.
     - Percentuais positivos/negativos.
   - **Armazenamento**: Guarda métricas em banco.

   🔗 **Endpoints**
   - `GET /metrics/{productId}`: Métricas agregadas.

   🧪 **Tecnologias**
   - Spring Boot.
   - Banco PostgreSQL.

### 3.1 Lógica de Cálculo das Métricas
   - **⭐ 1. Média das Avaliações**:
     - **Descrição**: Soma de todas as notas / total de avaliações.

   - **🔢 2. Número Total de Avaliações**:
     - **Descrição**: Quantidade de avaliações recebidas.

   - **📊 3. Distribuição das Notas**:
     - **Descrição**: Percentual de avaliações para cada nota de 1 a 5 estrelas.

   - **⚠️ 4. Percentual de Avaliações Negativas**:
     - **Descrição**: Percentual de avaliações abaixo de um limiar de 3 estrelas.

## 4. Guia de Configuração e Execução(Em Desenvolvimento)
- Instruções detalhadas para configurar e executar cada microserviço localmente.
