package com.project.feedbackservice.review.application.ports.output;

import com.project.feedbackservice.review.domain.Review;

public interface SaveReview {
    Review execute(Review review);
}

