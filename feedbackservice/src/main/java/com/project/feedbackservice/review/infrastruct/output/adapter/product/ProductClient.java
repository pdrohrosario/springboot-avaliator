package com.project.feedbackservice.review.infrastruct.output.adapter.product;

import com.project.feedbackservice.review.infrastruct.input.response.GetProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//TODO parametrizar
@FeignClient(name = "product-service", url = "http://localhost:8080", path = "/product")
public interface ProductClient {

    @GetMapping("/{id}")
    public ResponseEntity<GetProductResponse> getProductById(@PathVariable("id") String id);
}
