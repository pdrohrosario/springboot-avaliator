package com.project.feedbackservice.review.application.input;

import com.project.feedbackservice.review.domain.ProductId;

public record CreateReviewInput (String productId, Integer rating, String comment) {

}
