package com.project.feedbackservice.review.application.useCases;

import com.project.feedbackservice.review.application.input.CreateReviewInput;
import com.project.feedbackservice.review.application.mapper.ReviewUseCaseMapper;
import com.project.feedbackservice.review.application.output.CreateReviewOutput;
import com.project.feedbackservice.review.application.ports.input.CreateReview;
import com.project.feedbackservice.review.application.ports.output.FindProductById;
import com.project.feedbackservice.review.application.ports.output.PublishReviewCreatedEvent;
import com.project.feedbackservice.review.application.ports.output.SaveReview;
import com.project.feedbackservice.review.domain.ProductId;
import com.project.feedbackservice.review.domain.ProductIdIsNotValidException;
import com.project.feedbackservice.review.domain.ProductNotFoundException;
import com.project.feedbackservice.review.domain.Review;
import org.springframework.stereotype.Service;

@Service
public class CreateReviewUseCase implements CreateReview {

    private final SaveReview saveReview;
    private final FindProductById findProductById;
    private final PublishReviewCreatedEvent publishReviewCreatedEvent;

    public CreateReviewUseCase(SaveReview saveReview, FindProductById findProductById, PublishReviewCreatedEvent publishReviewCreatedEvent) {
        this.saveReview = saveReview;
        this.findProductById = findProductById;
        this.publishReviewCreatedEvent = publishReviewCreatedEvent;
    }

    @Override
    public CreateReviewOutput execute(CreateReviewInput input) {
        ProductId productId = validateProductId(input.productId());
        checkProductExistence(productId, input.productId());

        Review review = Review.create(productId, input.rating(), input.comment());

        review = saveReview.execute(review);
        publishReviewCreatedEvent.publish(review);

        return ReviewUseCaseMapper.toCreateOutput(review);
    }

    private ProductId validateProductId(String productIdString) {
        try {
            return ProductId.fromString(productIdString);
        } catch (NullPointerException ex) {
            throw new ProductIdIsNotValidException(productIdString);
        }
    }

    private void checkProductExistence(ProductId productId, String originalProductIdString) {
        if (!findProductById.execute(productId)) {
            throw new ProductNotFoundException(originalProductIdString);
        }
    }
}
