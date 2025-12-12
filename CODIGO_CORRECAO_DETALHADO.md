# 🚀 IMPLEMENTAÇÃO PASSO A PASSO - Corrigindo Violações

## TAREFA 1: Criar DTOs de Aplicação

### Arquivo 1: `CreateProductInput.java`
**Localização:** `application/input/CreateProductInput.java`

```java
package com.project.catalogservice.application.input;

import java.math.BigDecimal;

/**
 * Input para o caso de uso CreateProduct.
 * Representa os dados esperados pela camada de aplicação.
 * Agnóstico a HTTP, gRPC, ou qualquer outra forma de entrada.
 */
public record CreateProductInput(
    String name,
    BigDecimal price,
    String description,
    String category
) {
}
```

### Arquivo 2: `CreateProductOutput.java`
**Localização:** `application/output/CreateProductOutput.java`

```java
package com.project.catalogservice.application.output;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.project.catalogservice.domain.Product;

/**
 * Output do caso de uso CreateProduct.
 * Retorna dados da entidade de domínio de forma agnóstica a infrastructure.
 */
public record CreateProductOutput(
    Long id,
    String name,
    BigDecimal price,
    String description,
    String category,
    String status,
    LocalDate createdAt
) {
    public static CreateProductOutput fromDomain(Product product) {
        return new CreateProductOutput(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getDescription(),
            product.getCategory().name(),
            product.getStatus().name(),
            product.getCreatedAt()
        );
    }
}
```

---

## TAREFA 2: Atualizar Interface de Porto

**Arquivo:** `application/ports/input/CreateProduct.java`

```java
package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.application.input.CreateProductInput;
import com.project.catalogservice.application.output.CreateProductOutput;

/**
 * Porto de entrada (Input Port) para criação de produtos.
 * Define o contrato que qualquer implementação de caso de uso deve seguir.
 * 
 * Funciona com DTOs de aplicação, não com DTOs HTTP.
 */
public interface CreateProduct {
    CreateProductOutput execute(CreateProductInput input);
}
```

---

## TAREFA 3: Mover e Atualizar UseCase

**Arquivo:** `application/useCases/CreateProductUseCase.java`

Mude de: `application/ports/useCases/` para `application/useCases/`

```java
package com.project.catalogservice.application.useCases;

import com.project.catalogservice.application.input.CreateProductInput;
import com.project.catalogservice.application.output.CreateProductOutput;
import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.application.ports.output.FindProductByName;
import com.project.catalogservice.application.ports.output.SaveProduct;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.domain.ProductAlreadyExistsException;
import org.springframework.stereotype.Service;

/**
 * UseCase para criar um novo produto.
 * 
 * Orquestra a lógica de aplicação:
 * 1. Valida regras de negócio (verificar duplicação)
 * 2. Cria entidade de domínio (valida invariantes)
 * 3. Persiste usando portas de saída
 * 4. Retorna resultado sem conhecer HTTP
 * 
 * Dependências injetadas são sempre portas (interfaces),
 * nunca implementações específicas.
 */
@Service
public class CreateProductUseCase implements CreateProduct {

    private final SaveProduct saveProductPort;
    private final FindProductByName findProductByNamePort;

    public CreateProductUseCase(
        SaveProduct saveProductPort,
        FindProductByName findProductByNamePort) {
        this.saveProductPort = saveProductPort;
        this.findProductByNamePort = findProductByNamePort;
    }

    @Override
    public CreateProductOutput execute(CreateProductInput input) {
        // Verificar se produto com mesmo nome já existe
        if (findProductByNamePort.execute(input.name()) != null) {
            throw new ProductAlreadyExistsException(input.name());
        }

        // Criar entidade de domínio (aqui ocorrem validações de negócio)
        Product newProduct = Product.create(
            input.name(),
            input.price(),
            input.description(),
            input.category()
        );

        // Persistir produto
        newProduct = saveProductPort.execute(newProduct);

        // Retornar resultado formatado para aplicação
        return CreateProductOutput.fromDomain(newProduct);
    }
}
```

---

## TAREFA 4: Criar Mapper

**Arquivo:** `infrastructure/input/mappers/ProductControllerMapper.java`

```java
package com.project.catalogservice.infrastruct.input.mappers;

import com.project.catalogservice.application.input.CreateProductInput;
import com.project.catalogservice.application.output.CreateProductOutput;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;

/**
 * Mapper de camada de apresentação (HTTP).
 * Responsável por converter entre DTOs HTTP e DTOs de aplicação.
 * 
 * Mantém isolamento entre Infrastructure (HTTP) e Application.
 */
public class ProductControllerMapper {

    /**
     * Converte ProductRequest (DTO HTTP recebido) para CreateProductInput (DTO Application).
     * 
     * @param request DTO HTTP com dados do usuário
     * @return DTO de aplicação agnóstico a HTTP
     */
    public static CreateProductInput toCreateProductInput(ProductRequest request) {
        return new CreateProductInput(
            request.name(),
            request.price(),
            request.description(),
            request.category()
        );
    }

    /**
     * Converte CreateProductOutput (DTO Application) para ProductResponse (DTO HTTP).
     * 
     * @param output resultado do caso de uso
     * @return DTO HTTP para serializar em JSON
     */
    public static ProductResponse fromCreateProductOutput(CreateProductOutput output) {
        return new ProductResponse(
            output.id(),
            output.name(),
            output.price(),
            output.description(),
            output.category(),
            output.status(),
            output.createdAt()
        );
    }
}
```

---

## TAREFA 5: Atualizar Controller

**Arquivo:** `infrastructure/input/ProductController.java`

```java
package com.project.catalogservice.infrastruct.input;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.application.ports.input.GetProductById;
import com.project.catalogservice.application.ports.input.GetProductsByNameAndDescription;
import com.project.catalogservice.domain.validators.ValidateCreate;
import com.project.catalogservice.infrastruct.input.mappers.ProductControllerMapper;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;
import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para o domínio de Produtos.
 * 
 * Responsabilidades:
 * - Receber requisições HTTP
 * - Validar sintaxe HTTP (estrutura, tipos)
 * - Mapear para DTOs de aplicação
 * - Delegar para casos de uso
 * - Mapear resposta para HTTP
 * - Retornar ao cliente
 * 
 * NÃO conhece regras de negócio, apenas orquestra HTTP.
 */
@RestController
@RequestMapping("/product")
@Validated
public class ProductController {

    private final CreateProduct createProductUseCase;
    private final GetProductById getProductByIdUseCase;
    private final GetProductsByNameAndDescription getProductsByNameAndDescriptionUseCase;

    public ProductController(
        CreateProduct createProductUseCase,
        GetProductById getProductByIdUseCase,
        GetProductsByNameAndDescription getProductsByNameAndDescriptionUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.getProductsByNameAndDescriptionUseCase = getProductsByNameAndDescriptionUseCase;
    }

    /**
     * Cria um novo produto no catálogo.
     * 
     * Fluxo:
     * 1. ProductRequest é validado por @Validated e @NotBlank etc.
     * 2. Mapper converte ProductRequest → CreateProductInput
     * 3. UseCase executa lógica de negócio
     * 4. Mapper converte CreateProductOutput → ProductResponse
     * 5. Resposta é serializada como JSON
     * 
     * @param request DTO HTTP com dados do produto
     * @return ProductResponse com dados do produto criado
     */
    @PostMapping("/create")
    public ResponseEntity<ProductResponse> create(
        @RequestBody @Validated(ValidateCreate.class) ProductRequest request) {
        
        // 1. Mapear HTTP → Application
        var applicationInput = ProductControllerMapper.toCreateProductInput(request);
        
        // 2. Executar caso de uso
        var applicationOutput = createProductUseCase.execute(applicationInput);
        
        // 3. Mapear Application → HTTP
        var httpResponse = ProductControllerMapper.fromCreateProductOutput(applicationOutput);
        
        return new ResponseEntity<>(httpResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
        @PathVariable @Valid @NotNull(message = "ProductID is required") Long id) {
        return new ResponseEntity<>(getProductByIdUseCase.execute(id), HttpStatus.OK);
    }

    @GetMapping("/get-products")
    public ResponseEntity<PaginatedResponse<ProductResponse>> getByNameAndDescription(
        @RequestParam(name = "name")
        @NotBlank(message = "The 'name' field is required")
        String name,
        @RequestParam(name = "description")
        String description,
        @PageableDefault(size = 10, page = 0, sort = "name") Pageable pageable) {
        PaginatedResponse<ProductResponse> response = 
            getProductsByNameAndDescriptionUseCase.execute(name, description, pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
```

---

## TAREFA 6: Simplificar ProductRequest

**Arquivo:** `infrastructure/input/request/ProductRequest.java`

```java
package com.project.catalogservice.infrastruct.input.request;

import com.project.catalogservice.domain.validators.ValidateCreate;
import com.project.catalogservice.domain.validators.ValidateUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO HTTP para entrada de dados de produto.
 * 
 * Validações aqui são APENAS para estrutura HTTP:
 * - @NotNull/@NotBlank: campo é obrigatório?
 * - @Size/@Min/@Max: restrições de tamanho/formato HTTP
 * 
 * NUNCA deve conter validações de NEGÓCIO:
 * - Essas devem estar em Product.java (domínio)
 * - Ex: "Name must be entre 5-50 caracteres" é de negócio
 * 
 * Princípio: ProductRequest é agnóstico a regras de negócio,
 * apenas estrutura de dados.
 */
public record ProductRequest(
    @NotNull(groups = ValidateUpdate.class, message = "ID is required")
    Long id,

    @NotBlank(groups = {ValidateUpdate.class, ValidateCreate.class}, 
              message = "The 'name' field is required")
    String name,

    @NotNull(groups = {ValidateUpdate.class, ValidateCreate.class}, 
             message = "Price is required")
    BigDecimal price,

    String description,

    @NotBlank(groups = {ValidateUpdate.class, ValidateCreate.class}, 
              message = "Category is required")
    String category
) {
}
```

---

## RESUMO DAS MUDANÇAS

### Estrutura de Pastas Após Correção

```
application/
  ├── input/
  │   ├── CreateProductInput.java          ✨ NOVO
  │   └── GetProductByIdInput.java
  ├── output/
  │   ├── CreateProductOutput.java         ✨ NOVO
  │   └── GetProductByIdOutput.java
  ├── useCases/                            ✨ NOVA PASTA
  │   ├── CreateProductUseCase.java        ✨ MOVIDO de ports/useCases
  │   └── GetProductByIdUseCase.java
  └── ports/
      ├── input/
      │   ├── CreateProduct.java           🔄 ATUALIZADO
      │   └── GetProductById.java
      └── output/
          ├── SaveProduct.java
          ├── FindProductByName.java
          └── ...

infrastructure/
  ├── input/
  │   ├── ProductController.java           🔄 ATUALIZADO
  │   ├── mappers/                         ✨ NOVA PASTA
  │   │   └── ProductControllerMapper.java ✨ NOVO
  │   ├── request/
  │   │   └── ProductRequest.java          🔄 SIMPLIFICADO
  │   └── response/
  │       └── ProductResponse.java
  └── output/
      ├── entities/
      │   └── JpaProduct.java
      └── repositories/
          ├── ProductRepository.java
          ├── SaveProductAdapter.java
          └── FindProductByNameAdapter.java

domain/
  ├── Product.java                        ✓ SEM MUDANÇAS
  ├── ProductCategory.java
  ├── ProductStatus.java
  └── ProductAlreadyExistsException.java
```

---

## ORDEM DE IMPLEMENTAÇÃO RECOMENDADA

1. **Criar `CreateProductInput.java`** (5 min)
2. **Criar `CreateProductOutput.java`** (5 min)
3. **Atualizar `CreateProduct` interface** (3 min)
4. **Criar `ProductControllerMapper.java`** (10 min)
5. **Mover e atualizar `CreateProductUseCase`** (15 min)
6. **Atualizar `ProductController.create()`** (10 min)
7. **Simplificar `ProductRequest`** (5 min)
8. **Testar** (15 min)

**Tempo total:** ~65 minutos

---

## VERIFICAÇÃO APÓS IMPLEMENTAÇÃO

Execute este checklist para garantir que tudo está correto:

```
☐ CreateProductInput está em application/input/
☐ CreateProductOutput está em application/output/
☐ CreateProduct interface trabalha com CreateProductInput/Output
☐ CreateProductUseCase está em application/useCases/
☐ ProductControllerMapper converte ProductRequest → CreateProductInput
☐ ProductControllerMapper converte CreateProductOutput → ProductResponse
☐ ProductController usa mappers antes de chamar useCase
☐ ProductRequest não tem validações de negócio
☐ Projeto compila sem erros
☐ Testes passam
☐ Imports de infrastructure removidos de application
☐ UseCase não importa ProductRequest ou ProductResponse
☐ Controller não importa DTOs de application (usa apenas como passagem)
```

---

## APÓS IMPLEMENTAR ESTA TAREFA

Você terá:
✅ Separação clara entre HTTP e Application
✅ Domínio completamente isolado de infraestrutura
✅ Mappers bem definidos
✅ Possibilidade de mudar HTTP sem afetar negócio
✅ Testes mais fáceis (mocka DTOs de aplicação, não HTTP)
✅ Código mais limpo e testável


