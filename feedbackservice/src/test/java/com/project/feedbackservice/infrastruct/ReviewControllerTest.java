package com.project.feedbackservice.infrastruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.feedbackservice.review.application.input.CreateReviewInput;
import com.project.feedbackservice.review.application.output.CreateReviewOutput;
import com.project.feedbackservice.review.application.ports.input.CreateReview;
import com.project.feedbackservice.review.domain.ProductId;
import com.project.feedbackservice.review.domain.ReviewId;
import com.project.feedbackservice.review.infrastruct.input.ReviewController;
import com.project.feedbackservice.review.infrastruct.input.request.CreateReviewRequest;
import com.project.feedbackservice.review.infrastruct.input.response.CreateReviewResponse;
import com.project.feedbackservice.review.infrastruct.mapper.ReviewControllerMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateReview createReview;

    @Autowired
    private ObjectMapper objectMapper;

    CreateReviewRequest createRequest;

    CreateReviewResponse createResponse;

    CreateReviewOutput createOutput;

    @BeforeEach
    public void setup() {
        ReviewId reviewId = ReviewId.generate();
        ProductId productId = ProductId.generate();
        createRequest = new CreateReviewRequest(productId.toString(), 5, "Great product!");
        createOutput = new CreateReviewOutput(reviewId.toString(), productId.toString(),5, "Great product!", LocalDate.now());
        createResponse = ReviewControllerMapper.toResponse(createOutput);

    }

    @Test
    public void shouldCreateReviewAndReturnSuccess() throws Exception{
        //Arrange
        String request = objectMapper.writeValueAsString(createRequest);
        when(createReview.execute(any(CreateReviewInput.class))).thenReturn(createOutput);

        //Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/review/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(createResponse.reviewId()))
                .andExpect(jsonPath("$.productId").value(createResponse.productId()))
                .andExpect(jsonPath("$.rating").value(createResponse.rating()))
                .andExpect(jsonPath("$.comment").value(createResponse.comment()));
    }

    @Test
    public void shouldNotCreateReviewWhenProductIdIsMissing() throws Exception {
        //Arrange
        createRequest = new CreateReviewRequest("", 5, "Great product!");
        String request = objectMapper.writeValueAsString(createRequest);

        //Act
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/review/create")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(request))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        //Assert
        String messageFromValidation = objectMapper.readTree(response).get("message").asText();
        Assertions.assertTrue(messageFromValidation.contains("Product ID is required"));
    }

    @Test
    public void shouldNotCreateReviewWhenRatingIsMissing() throws Exception {
        //Arrange
        createRequest = new CreateReviewRequest(ProductId.generate().toString(), null, "Great product!");
        String request = objectMapper.writeValueAsString(createRequest);

        //Act
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/review/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        //Assert
        String messageFromValidation = objectMapper.readTree(response).get("message").asText();
        Assertions.assertTrue(messageFromValidation.contains("Rating is required"));
    }

}
