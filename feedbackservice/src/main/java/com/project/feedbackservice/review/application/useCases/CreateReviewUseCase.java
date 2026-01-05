package com.project.feedbackservice.review.application.useCases;

import com.project.feedbackservice.review.application.input.CreateReviewInput;
import com.project.feedbackservice.review.application.mapper.ReviewUseCaseMapper;
import com.project.feedbackservice.review.application.output.CreateReviewOutput;
import com.project.feedbackservice.review.application.ports.input.CreateReview;
import com.project.feedbackservice.review.application.ports.output.FindProductById;
import com.project.feedbackservice.review.application.ports.output.SaveReview;
import com.project.feedbackservice.review.domain.ProductId;
import com.project.feedbackservice.review.domain.ProductNotFoundException;
import com.project.feedbackservice.review.domain.Review;
import org.springframework.stereotype.Service;

@Service
public class CreateReviewUseCase implements CreateReview {

    private final SaveReview saveReview;
    private final FindProductById findProductById;

    public CreateReviewUseCase(SaveReview saveReview, FindProductById findProductById) {
        this.saveReview = saveReview;
        this.findProductById = findProductById;
    }

    @Override
    public CreateReviewOutput execute(CreateReviewInput input) {
        ProductId productId = ProductId.fromString(input.productId());

        if (!findProductById.execute(productId)){
            throw new ProductNotFoundException(input.productId());
        }

        Review review = Review.create(productId, input.rating(), input.comment());

        saveReview.execute(review);

        return ReviewUseCaseMapper.toCreateOutput(review);
    }
}
