package com.project.catalogservice.product.application.ports.output;


import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductId;

import java.util.Optional;

public interface FindById {
    Optional<Product> execute(ProductId id);
}
