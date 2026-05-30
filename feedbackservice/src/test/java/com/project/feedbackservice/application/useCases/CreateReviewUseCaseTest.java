package com.project.feedbackservice.application.useCases;

import com.project.feedbackservice.review.application.input.CreateReviewInput;
import com.project.feedbackservice.review.application.mapper.ReviewUseCaseMapper;
import com.project.feedbackservice.review.application.output.CreateReviewOutput;
import com.project.feedbackservice.review.application.ports.output.FindProductById;
import com.project.feedbackservice.review.application.ports.output.PublishReviewCreatedEvent;
import com.project.feedbackservice.review.application.ports.output.SaveReview;
import com.project.feedbackservice.review.application.useCases.CreateReviewUseCase;
import com.project.feedbackservice.review.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateReviewUseCaseTest {

    @InjectMocks
    private CreateReviewUseCase create;

    @Mock
    private SaveReview saveReview;

    @Mock
    private FindProductById findProductById;

    @Mock
    private PublishReviewCreatedEvent publishReviewCreatedEvent;

    Review review;

    CreateReviewInput createInput;

    CreateReviewOutput createOutput;

    @BeforeEach
    public void setup(){
        ProductId productId = ProductId.generate();
        ReviewId reviewId = ReviewId.generate();
        createInput = new CreateReviewInput(productId.toString(), 5, "Great product!");
        review = Review.fromEntity(reviewId, productId, 5, "Great product!", LocalDate.now());
        createOutput = ReviewUseCaseMapper.toCreateOutput(review);
    }

    @Test
    void shouldCreateReviewWithSuccess(){
        //Arrange
        when(findProductById.execute(any())).thenReturn(true);
        when(saveReview.execute(any())).thenReturn(review);

        //Act
        CreateReviewOutput response = create.execute(createInput);

        //Assert
        assertNotNull(response);
        assertEquals(createOutput, response);
        verify(publishReviewCreatedEvent).publish(any(Review.class));
    }

    @Test
    void shouldNotCreateReviewWhenProductIdNotExist(){
        //Arrange
        when(findProductById.execute(any())).thenReturn(false);

        //Act
        Exception exception = assertThrows(
                ProductNotFoundException.class,
                () -> create.execute(createInput)
        );

        //Assert
        assertEquals("Not found a Product with id: " + createInput.productId(), exception.getMessage());
        verify(publishReviewCreatedEvent, never()).publish(any(Review.class));

    }

    @Test
    void shouldNotCreateReviewWhenProductIdIsNull(){
        //Arrange
        createInput = new CreateReviewInput(null, 5, "Great product!");

        //Act
        Exception exception = assertThrows(
                ProductIdIsNotValidException.class,
                () -> create.execute(createInput)
        );

        //Assert
        assertEquals("Invalid Product ID: null", exception.getMessage());
        verify(publishReviewCreatedEvent, never()).publish(any());
    }

    @Test
    void shouldNotCreateReviewWhenRatingIsNegative(){
        //Arrange
        createInput = new CreateReviewInput(ProductId.generate().toString(), -1, "Great product!");
        when(findProductById.execute(any())).thenReturn(true);

        //Act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput)
        );

        //Assert
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
        verify(publishReviewCreatedEvent, never()).publish(any());
    }

    @Test
    void shouldNotCreateReviewWhenRatingIsGreaterThanFive(){
        //Arrange
        createInput = new CreateReviewInput(ProductId.generate().toString(), 6, "Great product!");
        when(findProductById.execute(any())).thenReturn(true);

        //Act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput));

        //Assert
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
        verify(publishReviewCreatedEvent, never()).publish(any());
    }

    @Test
    void shouldNotCreateReviewWhenCommentIsNull(){
        //Arrange
        createInput = new CreateReviewInput(ProductId.generate().toString(), 5, null);
        when(findProductById.execute(any())).thenReturn(true);

        //Act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput)
        );

        //Assert
        assertEquals("Comment cannot be null, empty or exceed 500 characters", exception.getMessage());
        verify(publishReviewCreatedEvent, never()).publish(any());
    }

    @Test
    void shouldNotCreateReviewWhenCommentIsLongerThan500Characters(){
        //Arrange
        String bigComment = "a".repeat(501);
        createInput = new CreateReviewInput(ProductId.generate().toString(), 5, bigComment);
        when(findProductById.execute(any())).thenReturn(true);

        //Act
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> create.execute(createInput)
        );

        //Assert
        assertEquals("Comment cannot be null, empty or exceed 500 characters", exception.getMessage());
        verify(publishReviewCreatedEvent, never()).publish(any());
    }

}
