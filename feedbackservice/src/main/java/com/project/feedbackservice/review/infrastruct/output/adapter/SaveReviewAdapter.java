package com.project.feedbackservice.review.infrastruct.output.adapter;

import com.project.feedbackservice.review.application.ports.output.SaveReview;
import com.project.feedbackservice.review.domain.Review;
import com.project.feedbackservice.review.domain.ReviewRepository;
import org.springframework.stereotype.Component;

@Component
public class SaveReviewAdapter implements SaveReview {

    private final ReviewRepository repository;

    public SaveReviewAdapter(ReviewRepository repository) {
        this.repository = repository;
    }

    @Override
    public Review execute(Review review) {
        return repository.save(review);
    }
}
