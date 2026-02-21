package com.project.feedbackservice.review.application.input;

public record CreateReviewInput (String productId, Integer rating, String comment) {

}
