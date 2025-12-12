# 🔧 PLANO DE CORREÇÃO PRÁTICO - Hexagonal & DDD

## PASSO 1: Criar DTOs de Aplicação (CRÍTICO)

### 1.1 Criar `CreateProductInput.java`

**Localização:** `application/input/CreateProductInput.java`

```java
package com.project.catalogservice.application.input;

import java.math.BigDecimal;

public record CreateProductInput(
    String name,
    BigDecimal price,
    String description,
    String category
) {
}
```

### 1.2 Criar `CreateProductOutput.java`

**Localização:** `application/output/CreateProductOutput.java`

```java
package com.project.catalogservice.application.output;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.project.catalogservice.domain.Product;

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

## PASSO 2: Corrigir a Interface de Porto

### 2.1 Atualizar `CreateProduct.java` (Porto de Entrada)

**ANTES (ERRADO):**
```java
public interface CreateProduct {
    ProductResponse execute(ProductRequest request);
}
```

**DEPOIS (CORRETO):**

```java
package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.application.input.CreateProductInput;
import com.project.catalogservice.application.output.CreateProductOutput;

public interface CreateProduct {
    CreateProductOutput execute(CreateProductInput input);
}
```

---

## PASSO 3: Atualizar o UseCase

### 3.1 Atualizar `CreateProductUseCase.java`

**ANTES (ERRADO):**
```java
package com.project.catalogservice.application.ports.useCases;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.application.ports.output.FindProductByName;
import com.project.catalogservice.application.ports.output.SaveProduct;
import com.project.catalogservice.domain.ProductAlreadyExistsException;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;  // ❌ ERRADO
import com.project.catalogservice.infrastruct.input.response.ProductResponse;  // ❌ ERRADO
import org.springframework.stereotype.Service;
import com.project.catalogservice.domain.Product;

@Service
public class CreateProductUseCase implements CreateProduct {
    private final SaveProduct save;
    private final FindProductByName findByName;

    public CreateProductUseCase(SaveProduct save, FindProductByName findByName) {
        this.save = save;
        this.findByName = findByName;
    }

    @Override
    public ProductResponse execute(ProductRequest request) {  // ❌ DTOs infrastructure
        if(null != findByName.execute(request.name())){
            throw new ProductAlreadyExistsException(request.name());
        }
        Product newProduct = Product.create(request.name(), request.price(), request.description(), request.category());
        newProduct = save.execute(newProduct);
        return ProductResponse.fromDomain(newProduct);
    }
}
```

**DEPOIS (CORRETO):**

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
 * Use Case para criar um novo produto no catálogo.
 * 
 * Responsabilidades:
 * - Validar se o produto já existe
 * - Criar a entidade de domínio
 * - Persistir no repositório
 * - Retornar resultado formatado para a aplicação
 */
@Service
public class CreateProductUseCase implements CreateProduct {

    private final SaveProduct saveProductPort;
    private final FindProductByName findProductByNamePort;

    public CreateProductUseCase(SaveProduct saveProductPort, FindProductByName findProductByNamePort) {
        this.saveProductPort = saveProductPort;
        this.findProductByNamePort = findProductByNamePort;
    }

    @Override
    public CreateProductOutput execute(CreateProductInput input) {
        // 1. Validar se produto já existe
        if (null != findProductByNamePort.execute(input.name())) {
            throw new ProductAlreadyExistsException(input.name());
        }

        // 2. Criar entidade de domínio (onde ocorrem as validações de negócio)
        Product newProduct = Product.create(
            input.name(),
            input.price(),
            input.description(),
            input.category()
        );

        // 3. Persistir usando porta de saída
        newProduct = saveProductPort.execute(newProduct);

        // 4. Retornar output (sem conhecer HTTP ou infrastructure)
        return CreateProductOutput.fromDomain(newProduct);
    }
}
```

---

## PASSO 4: Criar Mapper no Controller

### 4.1 Criar `ProductControllerMapper.java`

**Localização:** `infrastructure/input/mappers/ProductControllerMapper.java`

```java
package com.project.catalogservice.infrastruct.input.mappers;

import com.project.catalogservice.application.input.CreateProductInput;
import com.project.catalogservice.application.output.CreateProductOutput;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;

/**
 * Mapper responsável por converter entre objetos HTTP (Request/Response)
 * e objetos de Aplicação (Input/Output).
 * 
 * Mantém a separação entre as camadas:
 * - HTTP (Infrastructure) não conhece Application
 * - Application não conhece HTTP
 */
public class ProductControllerMapper {

    /**
     * Converte ProductRequest (DTO HTTP) para CreateProductInput (DTO Application)
     */
    public static CreateProductInput toApplicationInput(ProductRequest request) {
        return new CreateProductInput(
            request.name(),
            request.price(),
            request.description(),
            request.category()
        );
    }

    /**
     * Converte CreateProductOutput (DTO Application) para ProductResponse (DTO HTTP)
     */
    public static ProductResponse toHttpResponse(CreateProductOutput output) {
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

## PASSO 5: Atualizar Controller

### 5.1 Atualizar `ProductController.java`

**ANTES (ERRADO):**
```java
@PostMapping("/create")
public ResponseEntity<ProductResponse> create(@RequestBody @Validated(ValidateCreate.class) ProductRequest request) {
    return new ResponseEntity<>(createProduct.execute(request), HttpStatus.CREATED);
}
```

**DEPOIS (CORRETO):**

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
 * Controller responsável por receber requisições HTTP.
 * 
 * Responsabilidades:
 * - Validar entrada HTTP
 * - Mapear Request/Response
 * - Delegar para casos de uso da aplicação
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    private final CreateProduct createProduct;
    private final GetProductById getProductById;
    private final GetProductsByNameAndDescription getProductsByNameAndDescription;

    public ProductController(
        CreateProduct createProduct,
        GetProductById getProductById,
        GetProductsByNameAndDescription getProductsByNameAndDescription) {
        this.createProduct = createProduct;
        this.getProductById = getProductById;
        this.getProductsByNameAndDescription = getProductsByNameAndDescription;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> create(
        @RequestBody @Validated(ValidateCreate.class) ProductRequest request) {
        
        // 1. Mapear HTTP Request para Application Input
        var applicationInput = ProductControllerMapper.toApplicationInput(request);
        
        // 2. Executar caso de uso
        var applicationOutput = createProduct.execute(applicationInput);
        
        // 3. Mapear Application Output para HTTP Response
        var httpResponse = ProductControllerMapper.toHttpResponse(applicationOutput);
        
        return new ResponseEntity<>(httpResponse, HttpStatus.CREATED);
    }

    // ... resto dos métodos
}
```

---

## PASSO 6: Remover Validações Redundantes

### 6.1 Limpar `ProductRequest.java`

**ANTES (ERRADO - Validações misturadas):**
```java
public record ProductRequest(
    @NotNull(groups = ValidateUpdate.class, message = "ID is required") Long id,
    @NotBlank(groups = {ValidateUpdate.class, ValidateCreate.class}, message = "Name is required")
    @Size(groups = {ValidateUpdate.class, ValidateCreate.class}, min = 5, max = 50, message = "Name must be between 10 and 50 characters") 
    String name,
    @NotNull(groups = {ValidateUpdate.class, ValidateCreate.class}, message = "Price is required") 
    BigDecimal price,
    // ...
) {
}
```

**DEPOIS (CORRETO - Apenas validações HTTP/estruturais):**

```java
package com.project.catalogservice.infrastruct.input.request;

import com.project.catalogservice.domain.validators.ValidateCreate;
import com.project.catalogservice.domain.validators.ValidateUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO de entrada HTTP.
 * 
 * Validações aqui são apenas para estrutura/obrigatoriedade HTTP.
 * Validações de negócio devem estar em Product (domain).
 */
public record ProductRequest(
    @NotNull(groups = ValidateUpdate.class, message = "ID is required") 
    Long id,
    
    @NotBlank(groups = {ValidateUpdate.class, ValidateCreate.class}, message = "The 'name' field is required")
    String name,
    
    @NotNull(groups = {ValidateUpdate.class, ValidateCreate.class}, message = "Price is required") 
    BigDecimal price,
    
    String description,
    
    @NotBlank(groups = {ValidateUpdate.class, ValidateCreate.class}, message = "Category is required") 
    String category
) {
}
```

**Nota:** Remova `@Size` do ProductRequest, pois validações de tamanho são regras de negócio e devem estar em `Product.java`.

---

## PASSO 7: Documentação de Fluxo

### 7.1 Criar `FLUXO_ARQUITETURA.md`

```
FLUXO DE REQUISIÇÃO CORRETO:

1. HTTP Request chega no Controller
   ├─ ProductRequest (DTO HTTP) recebe validação @NotBlank, @NotNull
   └─ Validações estruturais/HTTP apenas

2. Controller mapeia para Application
   ├─ ProductControllerMapper.toApplicationInput(request)
   └─ CreateProductInput (DTO Application - domínio agnostic)

3. UseCase executa lógica de aplicação
   ├─ CreateProductUseCase.execute(input)
   ├─ Checa regras de negócio (FindProductByName)
   └─ Cria entidade de domínio

4. Entidade de Domínio valida regras de negócio
   ├─ Product.create(name, price, description, category)
   ├─ validateName() - regra de negócio
   ├─ validatePrice() - regra de negócio
   ├─ validateCategory() - regra de negócio
   └─ Retorna Product válido

5. UseCase persiste via Adapter
   ├─ SaveProductAdapter.execute(product)
   ├─ Converte para JpaProduct (entity banco)
   └─ Persiste e retorna Product

6. UseCase retorna resultado
   ├─ CreateProductOutput (DTO Application)
   └─ Contém apenas dados do domínio

7. Controller mapeia para HTTP Response
   ├─ ProductControllerMapper.toHttpResponse(output)
   └─ ProductResponse (DTO HTTP) é retornado

RESULTADO: Completa separação de responsabilidades!
```

---

## CHECKLIST DE IMPLEMENTAÇÃO

### Fase 1: DTOs de Aplicação (2-3 horas)
- [ ] Criar `CreateProductInput.java`
- [ ] Criar `CreateProductOutput.java`
- [ ] Criar `GetProductByIdInput.java` e `GetProductByIdOutput.java`
- [ ] Criar `GetProductsByNameAndDescriptionInput.java` e `GetProductsByNameAndDescriptionOutput.java`

### Fase 2: Mappers (1-2 horas)
- [ ] Criar `ProductControllerMapper.java`
- [ ] Criar mappers para outros use cases

### Fase 3: Correção de Portos (1 hora)
- [ ] Atualizar `CreateProduct` interface
- [ ] Atualizar `GetProductById` interface
- [ ] Atualizar `GetProductsByNameAndDescription` interface

### Fase 4: UsesCases (2-3 horas)
- [ ] Mover `CreateProductUseCase` para `application/useCases/`
- [ ] Atualizar para usar novos DTOs
- [ ] Criar/atualizar outros use cases

### Fase 5: Controller (1 hora)
- [ ] Atualizar `ProductController` com mappers

### Fase 6: Limpeza (1-2 horas)
- [ ] Remover imports desnecessários
- [ ] Remover validações redundantes
- [ ] Testes e validação

**Tempo Total Estimado:** 8-12 horas

---

## ANTES E DEPOIS VISUAL

### ANTES (❌ VIOLANDO HEXAGONAL)
```
ProductRequest (HTTP) 
    ↓ (sem mapeamento)
CreateProductUseCase
    ↓ (conhece HTTP)
Product (Domain)
    ↓ (confunde HTTP com negócio)
SaveProduct (Infrastructure)
```

### DEPOIS (✅ HEXAGONAL CORRETO)
```
ProductRequest (HTTP Input)
    ↓ mapper
CreateProductInput (Application)
    ↓ use case
Product (Domain - puro)
    ↓ adapter
JpaProduct (Infrastructure)
    ↓ mapper
CreateProductOutput (Application)
    ↓ mapper
ProductResponse (HTTP Output)
```

---


