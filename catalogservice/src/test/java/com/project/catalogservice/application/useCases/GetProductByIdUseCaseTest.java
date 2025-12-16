package com.project.catalogservice.application.useCases;

import com.project.catalogservice.product.application.mapper.ProductUseCaseMapper;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.application.ports.output.FindById;
import com.project.catalogservice.product.application.useCases.GetProductByIdUseCase;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductId;
import com.project.catalogservice.product.domain.ProductNotFound;
import org.assertj.core.api.Assertions;
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
public class GetProductByIdUseCaseTest {

    @InjectMocks
    private GetProductByIdUseCase get;;

    @Mock
    private FindById find;

    GetProductOutput productOutput;

    Product product;

    @BeforeEach
    public void setup() {
        product = Product.fromEntity(ProductId.generate(), "Product 1", BigDecimal.valueOf(100), "Description 1", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        productOutput = ProductUseCaseMapper.toGetOutput(product);
    }

    @Test
    void shouldGetProductByIdWithSuccess() {
       //arrange
        Optional<Product> search = Optional.of(product);
        when(find.execute(any())).thenReturn(search);

       //act
        GetProductOutput productFounded = get.execute(ProductId.generate().getValue());

       //assert
        assertNotNull(productFounded);
        assertEquals(productFounded, productOutput);
    }

    @Test
    void shouldNotFindProductByIdWhenProductNotExist() {
        //arrange
        ProductId productId = ProductId.generate();

        //act
        ProductNotFound exception = assertThrows(
                ProductNotFound.class,
                () -> get.execute(productId.getValue())
        );

        //assert
        assertEquals(String.format("Not found a Product with id: %s", productId.getValue()), exception.getMessage());
    }
}
