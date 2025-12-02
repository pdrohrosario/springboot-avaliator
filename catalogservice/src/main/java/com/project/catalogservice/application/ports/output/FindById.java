package com.project.catalogservice.application.ports.output;

import com.project.catalogservice.infrastruct.input.response.ProductResponse;

public interface FindById {
    ProductResponse execute(Long id);
}
