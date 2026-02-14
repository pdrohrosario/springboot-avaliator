package com.project.feedbackservice.review.infrastruct.output.adapter.product;

import com.project.feedbackservice.review.application.ports.output.FindProductById;
import com.project.feedbackservice.review.config.exception.ApiIntegrationException;
import com.project.feedbackservice.review.config.exception.ResourceNotFoundException;
import com.project.feedbackservice.review.domain.ProductId;
import com.project.feedbackservice.review.domain.ProductNotFoundException;

import org.hibernate.boot.beanvalidation.IntegrationException;

import org.springframework.stereotype.Component;

@Component
public class FindProductByIdAdapter implements FindProductById {

    private final ProductClient client;

    public FindProductByIdAdapter(ProductClient client) {
        this.client = client;
    }

    @Override
    public boolean execute(ProductId productId) {
        try {
            client.getProductById(productId.toString());
            return true;
        } catch (ResourceNotFoundException e) {
            throw new ProductNotFoundException(productId.toString());
        } catch (ApiIntegrationException e) {
            throw new IntegrationException(e.getMessage());
        }
    }
}
