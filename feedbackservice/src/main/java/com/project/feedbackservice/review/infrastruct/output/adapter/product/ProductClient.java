package com.project.feedbackservice.review.infrastruct.output.adapter.product;

import com.project.feedbackservice.review.infrastruct.input.response.GetProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "catalogservice", url = "http://catalogservice:8081")
public interface ProductClient {

    @GetMapping("/product/{id}")
    public ResponseEntity<GetProductResponse> getProductById(@PathVariable("id") String id);
}
