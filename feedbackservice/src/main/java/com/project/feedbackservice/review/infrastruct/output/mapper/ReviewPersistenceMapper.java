package com.project.feedbackservice.review.infrastruct.output.mapper;

import com.project.feedbackservice.review.domain.ProductId;
import com.project.feedbackservice.review.domain.Review;
import com.project.feedbackservice.review.domain.ReviewId;
import com.project.feedbackservice.review.infrastruct.output.entities.JpaReview;

public class ReviewPersistenceMapper {

    public static JpaReview toJpa(Review review){
        return new JpaReview(review.getId().getValue(),review.getProductId().getValue(), review.getRating(), review.getComment(), review.getCreatedAt());
    }

    public static Review toDomain(JpaReview jpaReview){
        return Review.fromEntity(ReviewId.fromString(jpaReview.getId().toString()), ProductId.fromString(jpaReview.getProductId().toString()), jpaReview.getRating(), jpaReview.getComment(), jpaReview.getCreateAt());
    }
}
