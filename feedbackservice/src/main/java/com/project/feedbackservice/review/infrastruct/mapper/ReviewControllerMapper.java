package com.project.feedbackservice.review.infrastruct.mapper;

import com.project.feedbackservice.review.application.input.CreateReviewInput;
import com.project.feedbackservice.review.application.output.CreateReviewOutput;
import com.project.feedbackservice.review.infrastruct.input.request.CreateReviewRequest;
import com.project.feedbackservice.review.infrastruct.input.response.CreateReviewResponse;

public class ReviewControllerMapper {

    public static CreateReviewInput toInput(CreateReviewRequest request){
        return new CreateReviewInput(
            request.productId(),
            request.rating(),
            request.comment()
        );
    }

    public static CreateReviewResponse toResponse(CreateReviewOutput output){
        return new CreateReviewResponse(
            output.reviewId(),
            output.productId(),
            output.rating(),
            output.comment(),
            output.createdAt()
        );
    }
}
