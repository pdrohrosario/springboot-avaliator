package com.project.catalogservice.application.useCases;

import com.project.catalogservice.application.ports.output.FindProductByName;
import com.project.catalogservice.application.ports.output.SaveProduct;
import com.project.catalogservice.application.ports.useCases.CreateProductUseCase;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.domain.ProductAlreadyExistsException;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
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

    ProductRequest request;

    ProductResponse response;

    @BeforeEach
    public void setup() {
        request = new ProductRequest(null, "Product 1", BigDecimal.valueOf(100), "Description 1", "ELECTRONICS");
        product = Product.fromEntity(1L, "Product 1", BigDecimal.valueOf(100), "Description 1", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        response = new ProductResponse(1L, "Product 1", BigDecimal.valueOf(100), "Description 1", "ELECTRONICS", "AVAILABLE", LocalDate.now());
    }

    @Test
    void shouldCreateProductWithSuccess() {
       //arrange
        when(saveProduct.execute(any())).thenReturn(product);

       //act
        ProductResponse productCreated = create.execute(request);

       //assert
        assertNotNull(productCreated);
        assertEquals(productCreated, response);
    }

    @Test
    void shouldNotCreateProductWhenNameIsNull() {
        //arrange
        request = new ProductRequest(null, null, BigDecimal.valueOf(100), "Description 1", "BOOKS");

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(request)
        );

        //assert
        assertEquals("Name cannot be null, empty or exceed 50 characters", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenPriceIsNull() {
        //arrange
        request = new ProductRequest(null, "Product 1", null, "Description 1", "SPORTS_EQUIPMENT");

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(request)
        );

        //assert
        assertEquals("Price cannot be null", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenPriceIsNegative() {
        //arrange
        request = new ProductRequest(null, "Product 1", new BigDecimal(-100), "Description 1", "TOYS");

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(request)
        );

        //assert
        assertEquals("Price cannot be negative", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenCategoryIsNull() {
        //arrange
        request = new ProductRequest(null, "Product 1", BigDecimal.valueOf(100), "Description 1", null);

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(request)
        );

        //assert
        assertEquals("Category cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenCategoryIsInvalid() {
        //arrange
        request = new ProductRequest(null, "Product 1", BigDecimal.valueOf(100), "Description 1", "INVALID_CATEGORY");

        //act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(request)
        );

        //assert
        assertEquals("Invalid category: INVALID_CATEGORY", exception.getMessage());
    }

    @Test
    void shouldNotCreateProductWhenAlreadyExistProductWithSameName() {
        //arrange
        when(findProductByName.execute("Product 1")).thenReturn(product);

        //act
        Exception exception = assertThrows(
                ProductAlreadyExistsException.class,
                () -> create.execute(request)
        );

        //assert
        assertEquals(String.format("Already exists a product saved with name %s.", request.name()), exception.getMessage());
    }
}
