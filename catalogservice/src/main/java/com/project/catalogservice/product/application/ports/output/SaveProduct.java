package com.project.catalogservice.product.application.ports.output;

import com.project.catalogservice.product.domain.Product;

public interface SaveProduct {
    Product execute(Product product);
}
