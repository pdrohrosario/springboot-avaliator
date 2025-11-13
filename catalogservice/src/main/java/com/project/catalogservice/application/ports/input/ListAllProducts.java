package com.project.catalogservice.application.ports.input;

import java.util.List;

import com.project.catalogservice.domain.Product;

public interface ListAllProducts {
    List<Product> execute();
}
