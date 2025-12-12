package com.project.catalogservice.application.useCases;

import com.project.catalogservice.product.application.input.GetProductsByNameAndDescriptionInput;
import com.project.catalogservice.product.application.mapper.ProductUseCaseMapper;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.application.ports.output.FindProductsByNameAndDescription;
import com.project.catalogservice.product.application.useCases.GetProductsByUsernameAndDescriptionUseCase;
import com.project.catalogservice.product.common.output.PaginatedResponse;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetProductsByNameAndDescriptionUseCaseTest {

    @InjectMocks
    private GetProductsByUsernameAndDescriptionUseCase getProductsByNameAndDescription;

    @Mock
    private FindProductsByNameAndDescription findProductsByNameAndDescription;

    GetProductsByNameAndDescriptionInput input;

    PaginatedResponse<GetProductOutput> paginatedGetProductOutput;

    PaginatedResponse<Product> paginatedProducts;

    Pageable pageable;

    @BeforeEach
    public void setup() {
        List<Product> productList = IntStream.range(0,10)
                .mapToObj(i -> Product.fromEntity(ProductId.generate(),"Book " + i, BigDecimal.TEN,"new book " + i, "BOOKS" , "AVALIABE" ,LocalDate.now()))
                .toList();

        pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        paginatedProducts = new PaginatedResponse<>(productList, pageable.getPageNumber(), false);

        paginatedGetProductOutput = new PaginatedResponse<>(productList.stream().map(ProductUseCaseMapper::toGetOutput).toList(), pageable.getPageNumber(), false);

        input = new GetProductsByNameAndDescriptionInput("Book","new book", pageable.getPageNumber(), pageable.getPageSize(), "name");
    }

    @Test
    void shouldReturnListWith10ProductsWithSuccess() {
        //arrange
        when(findProductsByNameAndDescription.execute(any())).thenReturn(paginatedProducts);

        //act
        PaginatedResponse<GetProductOutput> response = getProductsByNameAndDescription.execute(input);

        //assert
        Assertions.assertEquals(response.items().size(), paginatedGetProductOutput.items().size());
        Assertions.assertFalse(response.hasNextPage());
        Assertions.assertEquals(response.currentPage(), pageable.getPageNumber());
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() {
        //arrange
        paginatedProducts = new PaginatedResponse<>(List.of(), pageable.getPageNumber(), false);
        when(findProductsByNameAndDescription.execute(any())).thenReturn(paginatedProducts);

        //act
        PaginatedResponse<GetProductOutput> response = getProductsByNameAndDescription.execute(input);

        //assert
        Assertions.assertEquals(0, response.items().size());
        Assertions.assertFalse(response.hasNextPage());
    }

}
