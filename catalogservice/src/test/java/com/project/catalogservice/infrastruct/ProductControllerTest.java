package com.project.catalogservice.infrastruct;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import com.project.catalogservice.application.ports.input.GetById;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import com.project.catalogservice.infrastruct.output.repositories.ProductRepository;
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
    private GetById getById;

    @Autowired
    private ObjectMapper objectMapper;

    ProductRequest request;

    Product product;

    ProductResponse response;

    @BeforeEach
    public void setup() {
        request = new ProductRequest(null,"Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS");
        product = Product.fromEntity( 1L,"Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS", "AVAILABLE", LocalDate.now());
        response = ProductResponse.fromDomain(product);
    }
    
    @Test
    public void shouldCreateProductAndReturnSuccess() throws Exception {
        // Arrange
        String request = objectMapper.writeValueAsString(new ProductRequest(1L, "Laptop", BigDecimal.valueOf(99.99), "It is a new HP Laptop", "ELECTRONICS"));
        when(createProduct.execute(any())).thenReturn(product);

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
        when(getById.execute(any())).thenReturn(product);

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
        when(getById.execute(any())).thenReturn(null);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/product/%d",product.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$").doesNotExist());
    }

}
