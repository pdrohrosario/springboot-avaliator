package com.project.feedbackservice.review.infrastruct.output.repository;

import com.project.feedbackservice.review.common.output.PaginatedResponse;
import com.project.feedbackservice.review.domain.ProductId;
import com.project.feedbackservice.review.domain.Review;
import com.project.feedbackservice.review.domain.ReviewId;
import com.project.feedbackservice.review.domain.ReviewRepository;
import com.project.feedbackservice.review.infrastruct.output.entities.JpaReview;
import com.project.feedbackservice.review.infrastruct.output.mapper.ReviewPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReviewRepositoryImpl implements ReviewRepository {

    private final JpaReviewRepository repository;

    public ReviewRepositoryImpl(JpaReviewRepository repository) {
        this.repository = repository;
    }

    @Override
    public Review save(Review review) {
        JpaReview jpa = ReviewPersistenceMapper.toJpa(review);
        jpa = repository.save(jpa);
        return ReviewPersistenceMapper.toDomain(jpa);
    }

    @Override
    public Optional<Review> findById(ReviewId id) {
        return Optional.empty();
    }

    @Override
    public PaginatedResponse<Review> findReviewsByProductId(ProductId productId, int page, int size) {
        return null;
    }
}
