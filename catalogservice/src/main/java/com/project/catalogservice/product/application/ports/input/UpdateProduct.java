package com.project.catalogservice.product.application.ports.input;

import com.project.catalogservice.product.domain.Product;

public interface UpdateProduct {
    Product execute(Product product);
}
