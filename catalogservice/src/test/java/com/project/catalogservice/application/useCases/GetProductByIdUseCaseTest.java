package com.project.catalogservice.application.useCases;

import com.project.catalogservice.application.ports.output.FindById;
import com.project.catalogservice.application.ports.useCases.GetProductByIdUseCase;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetProductByIdUseCaseTest {

    @InjectMocks
    private GetProductByIdUseCase get;;

    @Mock
    private FindById find;

    Product product;

    ProductResponse response;

    @BeforeEach
    public void setup() {
        product = Product.fromEntity(1L, "Product 1", BigDecimal.valueOf(100), "Description 1", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        response = ProductResponse.fromDomain(product);
    }

    @Test
    void shouldGetProductByIdWithSuccess() {
       //arrange
        when(find.execute(1L)).thenReturn(product);

       //act
        ProductResponse productFounded = get.execute(1L);

       //assert
        assertNotNull(productFounded);
        assertEquals(productFounded, response);
    }

    @Test
    void shouldNotFindProductByIdWhenProductNotExist() {

        //act
        ProductResponse productFounded = get.execute(1L);

        //assert
        assertNull(productFounded);
    }
}
