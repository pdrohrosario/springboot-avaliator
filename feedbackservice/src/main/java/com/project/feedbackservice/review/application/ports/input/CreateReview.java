package com.project.feedbackservice.review.application.ports.input;

import com.project.feedbackservice.review.application.input.CreateReviewInput;
import com.project.feedbackservice.review.application.output.CreateReviewOutput;

public interface CreateReview {
    CreateReviewOutput execute(CreateReviewInput input);
}
