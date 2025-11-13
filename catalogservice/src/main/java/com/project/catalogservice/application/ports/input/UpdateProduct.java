package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.domain.Product;

public interface UpdateProduct {
    Product execute(Product product);
}
