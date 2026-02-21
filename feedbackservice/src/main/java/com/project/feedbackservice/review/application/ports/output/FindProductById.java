package com.project.feedbackservice.review.application.ports.output;

import com.project.feedbackservice.review.domain.ProductId;

public interface FindProductById {
    boolean execute(ProductId productId);
}
