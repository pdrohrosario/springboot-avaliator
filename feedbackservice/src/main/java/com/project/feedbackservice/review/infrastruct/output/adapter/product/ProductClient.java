package com.project.feedbackservice.review.infrastruct.output.adapter.product;

import com.project.feedbackservice.review.infrastruct.input.response.GetProductResponse;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogservice", url = "http://catalogservice:8081", path = "/product", configuration = FeignAutoConfiguration.class)
public interface ProductClient {

    @GetMapping("/{id}")
    public ResponseEntity<GetProductResponse> getProductById(@PathVariable("id") String id);
}
