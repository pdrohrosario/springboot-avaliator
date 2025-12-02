package com.project.catalogservice.application.useCases;

import com.project.catalogservice.application.ports.output.FindProductsByNameAndDescription;
import com.project.catalogservice.application.ports.useCases.GetProductsByUsernameAndDescriptionUseCase;
import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
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

    PaginatedResponse<ProductResponse> paginatedResponse;

    List<ProductResponse> bookList;

    Page<ProductResponse> productsFound;

    Pageable pageable;

    @BeforeEach
    public void setup() {
        bookList = IntStream.range(0,10)
                .mapToObj(i -> new ProductResponse(Integer.toUnsignedLong(i),"Book " + i, BigDecimal.TEN,"new book " + i, "BOOKS" , "AVALIABE" ,LocalDate.now()))
                .toList();

        pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        productsFound = new PageImpl<>(bookList, pageable, bookList.size());

        paginatedResponse = new PaginatedResponse<>(bookList, pageable.getPageNumber(), false);

    }

    @Test
    void shouldReturnListWith10ProductsWithSuccess() {
        //arrange

        when(findProductsByNameAndDescription.execute(any(), any(), any())).thenReturn(paginatedResponse);

        //act
        paginatedResponse = getProductsByNameAndDescription.execute("Book","new book", pageable);

        //assert
        Assertions.assertEquals(paginatedResponse.items().size(), bookList.size());
        Assertions.assertFalse(paginatedResponse.hasNextPage());
        Assertions.assertEquals(paginatedResponse.currentPage(), pageable.getPageNumber());
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() {
        //arrange
        paginatedResponse = new PaginatedResponse<>(List.of(), pageable.getPageNumber(), false);
        when(findProductsByNameAndDescription.execute(any(), any(), any())).thenReturn(paginatedResponse);

        //act
        paginatedResponse = getProductsByNameAndDescription.execute("Book","new book", pageable);

        //assert
        Assertions.assertEquals(0, paginatedResponse.items().size());
        Assertions.assertFalse(paginatedResponse.hasNextPage());
    }

    @Test
    void shouldReturnTenProductWhenPageableIsNull() {
        //arrange
        when(findProductsByNameAndDescription.execute(any(), any(), any())).thenReturn(paginatedResponse);

        //act
        paginatedResponse = getProductsByNameAndDescription.execute("Book","new book", null);

        //assert
        Assertions.assertEquals(10, paginatedResponse.items().size());
        Assertions.assertFalse(paginatedResponse.hasNextPage());
    }

}
