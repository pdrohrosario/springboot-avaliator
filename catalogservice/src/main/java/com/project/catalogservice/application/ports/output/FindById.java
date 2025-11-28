package com.project.catalogservice.application.ports.output;

import com.project.catalogservice.domain.Product;

public interface FindById {
    Product execute(Long id);
}
