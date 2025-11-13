package com.project.catalogservice.application.ports.output;

import com.project.catalogservice.domain.Product;

public interface SaveProduct {
    Product execute(Product product);
}
