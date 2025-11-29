package com.project.catalogservice.infrastruct;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.project.catalogservice.application.ports.input.GetProductById;
import com.project.catalogservice.application.ports.input.ListAllProducts;
import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.input.ProductController;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProduct createProduct;

    @MockitoBean
    private GetProductById getProductById;

    @MockitoBean
    private ListAllProducts listAllProducts;

    @Autowired
    private ObjectMapper objectMapper;

    ProductRequest request;

    Product product;

    ProductResponse response;

    @BeforeEach
    public void setup() {
        request = new ProductRequest(null,"Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS");
        product = Product.fromEntity( 1L,"Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        response = new ProductResponse(1L,"Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS", "AVAILABLE", LocalDate.now());
    }
    
    @Test
    public void shouldCreateProductAndReturnSuccess() throws Exception {
        // Arrange
        String request = objectMapper.writeValueAsString(new ProductRequest(1L, "Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS"));
        when(createProduct.execute(any())).thenReturn(response);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/product/create")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(request))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.name").value(response.name()))
        .andExpect(jsonPath("$.price").value(response.price()))
        .andExpect(jsonPath("$.description").value(response.description()))
        .andExpect(jsonPath("$.category").value(response.category()));
    }

    @Test
    public void shouldReturnBadRequestWhenRequestIsMissing() throws Exception {
        // Act and Assert
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/product/create")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .characterEncoding(StandardCharsets.UTF_8)
                                    .content("{}"))
                                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
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
        .andExpect(MockMvcResultMatchers.status().isOk())
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
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void shouldReturnTenProducts() throws Exception {
        //Arrange
        List<ProductResponse> productList = IntStream.range(0, 10)
                .mapToObj(i -> new ProductResponse((long) i, "Product" + i, BigDecimal.valueOf(10 + i), "Description" + i, "BOOKS", "AVAILABLE", LocalDate.now()))
                .collect(Collectors.toList());

        PaginatedResponse<ProductResponse> paginatedResponse = new PaginatedResponse<>(productList, 1, false);

        when(listAllProducts.execute()).thenReturn(paginatedResponse);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/product/search-products")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.items.size()").value(paginatedResponse.items().size()));
    }

    @Test
    public void shouldReturnEmptyListWhenNoProductsExist() throws Exception {
        //Arrange
        PaginatedResponse<ProductResponse> emptyResponse = new PaginatedResponse<>(null, 0, false);

        when(listAllProducts.execute()).thenReturn(emptyResponse);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/product/search-products")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.items").isEmpty())
        .andExpect(jsonPath("$.hasNextPage").value("false"));
    }
}
