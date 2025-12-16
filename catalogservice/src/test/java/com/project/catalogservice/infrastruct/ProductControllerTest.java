package com.project.catalogservice.infrastruct;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import com.project.catalogservice.product.application.output.CreateProductOutput;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.application.ports.input.GetProductById;
import com.project.catalogservice.product.application.ports.input.GetProductsByNameAndDescription;
import com.project.catalogservice.product.common.output.PaginatedResponse;
import com.project.catalogservice.product.domain.ProductAlreadyExistsException;
import com.project.catalogservice.product.domain.ProductId;
import com.project.catalogservice.product.domain.ProductNotFound;
import com.project.catalogservice.product.infrastruct.input.input.mapper.ProductControllerMapper;
import com.project.catalogservice.product.infrastruct.input.input.request.UpdateProductRequest;
import com.project.catalogservice.product.infrastruct.input.input.response.CreateProductResponse;
import com.project.catalogservice.product.infrastruct.input.input.response.GetProductResponse;
import com.project.catalogservice.product.infrastruct.input.input.response.UpdateProductResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.project.catalogservice.product.application.ports.input.CreateProduct;
import com.project.catalogservice.product.infrastruct.input.input.ProductController;
import com.project.catalogservice.product.infrastruct.input.input.request.CreateProductRequest;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProduct createProduct;

    @MockitoBean
    private GetProductById getProductById;

    @MockitoBean
    private GetProductsByNameAndDescription getProductsByNameAndDescription;

    @Autowired
    private ObjectMapper objectMapper;

    CreateProductRequest createRequest;

    CreateProductResponse createResponse;

    CreateProductOutput createProductOutput;

    UpdateProductRequest updateRequest;

    UpdateProductResponse updateResponse;

    GetProductOutput getProductOutput;

    GetProductResponse getResponse;

    @BeforeEach
    public void setup() {
        createRequest = new CreateProductRequest("Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS");
        createProductOutput = new CreateProductOutput(ProductId.generate().getValue(), "Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        createResponse = ProductControllerMapper.toResponse(createProductOutput);
        getProductOutput = new GetProductOutput(ProductId.generate().getValue(),"Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        getResponse = ProductControllerMapper.toResponse(getProductOutput);
    }
    
    @Test
    public void shouldCreateProductAndReturnSuccess() throws Exception {
        // Arrange
        String request = objectMapper.writeValueAsString(new CreateProductRequest( "Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS"));
        when(createProduct.execute(any())).thenReturn(createProductOutput);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/product/create")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(createResponse.id()))
        .andExpect(jsonPath("$.name").value(createResponse.name()))
        .andExpect(jsonPath("$.price").value(createResponse.price()))
        .andExpect(jsonPath("$.description").value(createResponse.description()))
        .andExpect(jsonPath("$.category").value(createResponse.category()));
    }

    @Test
    public void shouldReturnBadRequestWhenRequestIsMissing() throws Exception {
        // Act and Assert
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/product/create")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .characterEncoding(StandardCharsets.UTF_8)
                                    .content("{}"))
                                    .andExpect(status().isBadRequest())
                                    .andReturn().getResponse().getContentAsString();

        String messageFromValidation = objectMapper.readTree(response).get("message").asText();
        Assertions.assertTrue(messageFromValidation.contains("Name is required"));
        Assertions.assertTrue(messageFromValidation.contains("Category is required"));
        Assertions.assertTrue(messageFromValidation.contains("Price is required"));
    }

    @Test
    public void shouldReturnProductWhenIdisNotNull() throws Exception {
        // Arrange
        when(getProductById.execute(any())).thenReturn(getProductOutput);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/product/%d",1L))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(getResponse.id()))
        .andExpect(jsonPath("$.name").value(getResponse.name()))
        .andExpect(jsonPath("$.price").value(getResponse.price()))
        .andExpect(jsonPath("$.description").value(getResponse.description()))
        .andExpect(jsonPath("$.category").value(getResponse.category()));
    }

    @Test
    public void shouldNotReturnProductWhenIdNotExist() throws Exception {
        // Arrange
        ProductId productId = ProductId.generate();
        when(getProductById.execute(any())).thenThrow(new ProductNotFound(productId.getValue()));

        // Act and Assert
        String response = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/product/%s",productId.getValue()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                        .andReturn().getResponse().getContentAsString();

        String messageFromValidation = objectMapper.readTree(response).get("message").asText();
        Assertions.assertTrue(messageFromValidation.contains(String.format("Not found a Product with id: %s", productId.getValue())));
    }

    @Test
    public void shouldReturnTenProductWithSuccess() throws Exception {
        //Arrange
         List<GetProductOutput> productList = (IntStream.range(0, 10)
                .mapToObj(i -> new GetProductOutput(ProductId.generate().getValue(), "Book " + i, BigDecimal.valueOf(10 + i), "A new book " + i, "BOOKS", "AVAILABLE", LocalDate.now()))
                .toList());

        PaginatedResponse<GetProductOutput> paginatedResponse = new PaginatedResponse<>(productList, 0, false);

        when(getProductsByNameAndDescription.execute(any())).thenReturn(paginatedResponse);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/product/get-products?name=Book&description=new%book&page=0&size=10&sort=name")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.size()").value(paginatedResponse.items().size()));
    }

    @Test
    public void shouldReturnEmptyListWhenNoProductsExist() throws Exception {
        //Arrange
        PaginatedResponse<GetProductOutput> paginatedResponse = new PaginatedResponse<>(null, 0, false);

        when(getProductsByNameAndDescription.execute(any())).thenReturn(paginatedResponse);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/product/get-products?name=Book&description=new%book&page=0&size=10&sort=name")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty())
        .andExpect(jsonPath("$.hasNextPage").value("false"));
    }
}
