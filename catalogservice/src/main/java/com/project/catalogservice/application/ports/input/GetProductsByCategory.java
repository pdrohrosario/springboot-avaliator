package com.project.catalogservice.application.ports.input;

public interface GetCategoryAndOrderByCreatedDate {
    List<Product> execute(String category);
}
