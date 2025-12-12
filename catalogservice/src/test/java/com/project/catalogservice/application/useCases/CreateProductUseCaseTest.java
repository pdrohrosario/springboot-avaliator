package com.project.catalogservice.application.useCases;

import com.project.catalogservice.product.application.mapper.ProductUseCaseMapper;
import com.project.catalogservice.product.application.input.CreateProductInput;
import com.project.catalogservice.product.application.output.CreateProductOutput;
import com.project.catalogservice.product.application.ports.output.FindProductByName;
import com.project.catalogservice.product.application.ports.output.SaveProduct;
import com.project.catalogservice.product.application.useCases.CreateProductUseCase;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductAlreadyExistsException;
import com.project.catalogservice.product.domain.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateProductUseCaseTest {

    @InjectMocks
    private CreateProductUseCase create;

    @Mock
    private SaveProduct saveProduct;

    @Mock
    private FindProductByName findProductByName;

    Product product;

    CreateProductInput createInput;

    CreateProductOutput createOutput;

    @BeforeEach
    public void setup() {
        createInput = new CreateProductInput("Product 1", BigDecimal.valueOf(100), "Description 1", "ELECTRONICS");
        product = Product.fromEntity(ProductId.generate(), "Product 1", BigDecimal.valueOf(100), "Description 1", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        createOutput = ProductUseCaseMapper.toCreateOutput(product);
    }

    @Test
    void shouldCreateProductWithSuccess() {
       //arrange
        when(saveProduct.execute(any())).thenReturn(product);

       //act
        CreateProductOutput response= create.execute(createInput);

       //assert
        assertNotNull(response);
        assertEquals(createOutput, response);
    }

    @Test
    void shouldNotCreateProductWhenNameIsNull() {
        //arrange
        createInput = new CreateProductInput( null, BigDecimal.valueOf(100), "Description 1", "BOOKS");

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput)
        );

        //assert
        assertEquals("Name cannot be null, empty or exceed 50 characters", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenPriceIsNull() {
        //arrange
        createInput = new CreateProductInput("Product 1", null, "Description 1", "SPORTS_EQUIPMENT");

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput)
        );

        //assert
        assertEquals("Price cannot be null", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenPriceIsNegative() {
        //arrange
        createInput = new CreateProductInput( "Product 1", new BigDecimal(-100), "Description 1", "TOYS");

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput)
        );

        //assert
        assertEquals("Price cannot be negative", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenCategoryIsNull() {
        //arrange
        createInput = new CreateProductInput("Product 1", BigDecimal.valueOf(100), "Description 1", null);

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput)
        );

        //assert
        assertEquals("Category cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenCategoryIsInvalid() {
        //arrange
        createInput = new CreateProductInput( "Product 1", BigDecimal.valueOf(100), "Description 1", "INVALID_CATEGORY");

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput)
        );

        //assert
        assertEquals("Invalid category: INVALID_CATEGORY", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenAlreadyExistProductWithSameName() {
        //arrange
        Optional<Product> search = Optional.of(product);
        when(findProductByName.execute("Product 1")).thenReturn(search);

        //act
        Exception exception = assertThrows(
                ProductAlreadyExistsException.class,
                () -> create.execute(createInput)
        );

        //assert
        assertEquals(String.format("Already exists a product saved with name %s.", createInput.name()), exception.getMessage());
    }
}
