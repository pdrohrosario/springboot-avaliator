package com.project.feedbackservice.review.application.mapper;

import com.project.feedbackservice.review.application.output.CreateReviewOutput;
import com.project.feedbackservice.review.domain.Review;

public class ReviewUseCaseMapper {

    public static CreateReviewOutput toCreateOutput(Review review){
        return new CreateReviewOutput(review.getId().toString(), review.getProductId().toString(), review.getRating(), review.getComment(), review.getCreatedAt());
    }
}
