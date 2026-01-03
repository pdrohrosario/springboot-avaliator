package com.project.feedbackservice.review.infrastruct.output.adapter.product;

import com.project.feedbackservice.review.application.ports.output.FindProductById;
import com.project.feedbackservice.review.domain.ProductId;
import com.project.feedbackservice.review.infrastruct.input.response.GetProductResponse;
import org.springframework.http.ResponseEntity;

public class FindProductByIdAdapter implements FindProductById {

    private final ProductClient client;

    public FindProductByIdAdapter(ProductClient client) {
        this.client = client;
    }

    @Override
    public boolean execute(ProductId productId) {
        ResponseEntity<GetProductResponse> response = client.getProductById(productId.toString());
        return  response.getBody() != null && response.getBody().id() != null;
    }
}
