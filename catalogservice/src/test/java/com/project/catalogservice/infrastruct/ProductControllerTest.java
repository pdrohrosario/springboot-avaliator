package com.project.catalogservice.infrastruct;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.project.catalogservice.application.ports.input.GetProductById;
import com.project.catalogservice.application.ports.input.GetProductsByNameAndDescription;
import com.project.catalogservice.application.ports.output.CreateProductOutput;
import com.project.catalogservice.infrastruct.product.input.request.UpdateProductRequest;
import com.project.catalogservice.infrastruct.product.input.response.CreateProductResponse;
import com.project.catalogservice.infrastruct.product.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.product.input.response.ProductResponse;
import com.project.catalogservice.infrastruct.product.input.response.UpdateProductResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.product.input.ProductController;
import com.project.catalogservice.infrastruct.product.input.request.CreateProductRequest;

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

    CreateProductOutput serviceOutputResponse;

    UpdateProductRequest updateRequest;

    UpdateProductResponse updateResponse;

    @BeforeEach
    public void setup() {
        createRequest = new CreateProductRequest("Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS");
        createResponse = new CreateProductResponse(1L,"Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        serviceOutputResponse = new CreateProductOutput(1L,"Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS", "AVAILABLE", LocalDate.now());
    }
    
    @Test
    public void shouldCreateProductAndReturnSuccess() throws Exception {
        // Arrange
        String request = objectMapper.writeValueAsString(new CreateProductRequest( "Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS"));
        when(createProduct.execute(any())).thenReturn(serviceOutputResponse);

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
        when(getProductById.execute(any())).thenReturn(response);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/product/%d",product.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.name").value(response.name()))
        .andExpect(jsonPath("$.price").value(response.price()))
        .andExpect(jsonPath("$.description").value(response.description()))
        .andExpect(jsonPath("$.category").value(response.category()));
    }

    @Test
    public void shouldNotReturnProductWhenIdNotExist() throws Exception {
        // Arrange
        when(getProductById.execute(any())).thenReturn(null);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/product/%d",product.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void shouldReturnTenProductWithSuccess() throws Exception {
        //Arrange
        List<ProductResponse> productList = IntStream.range(0, 10)
                .mapToObj(i -> new ProductResponse((long) i, "Book " + i, BigDecimal.valueOf(10 + i), "A new book " + i, "BOOKS", "AVAILABLE", LocalDate.now()))
                .collect(Collectors.toList());

        PaginatedResponse<ProductResponse> paginatedResponse = new PaginatedResponse<>(productList, 1, false);

        when(getProductsByNameAndDescription.execute(any(),any(),any())).thenReturn(paginatedResponse);

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
        PaginatedResponse<ProductResponse> emptyResponse = new PaginatedResponse<>(null, 0, false);

        when(getProductsByNameAndDescription.execute(any(), any(), any())).thenReturn(emptyResponse);

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
