package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.domain.Product;

public interface GetById {
    Product execute(Long id);
}
