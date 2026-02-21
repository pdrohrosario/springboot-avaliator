package com.project.feedbackservice.review.domain;

import com.project.feedbackservice.review.common.output.PaginatedResponse;

import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(ReviewId id);
    PaginatedResponse<Review> findReviewsByProductId(ProductId productId, int page, int size);
}
