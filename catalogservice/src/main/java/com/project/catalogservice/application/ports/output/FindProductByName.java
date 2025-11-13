package com.project.catalogservice.application.ports.output;

import com.project.catalogservice.domain.Product;

import java.util.Optional;

public interface FindProductByName {
    Product execute(String name);
}
