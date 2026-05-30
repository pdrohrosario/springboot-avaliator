package com.project.feedbackservice.review.application.ports.output;

import com.project.feedbackservice.review.domain.Review;

public interface PublishReviewCreatedEvent {
    void publish(Review review);
}
