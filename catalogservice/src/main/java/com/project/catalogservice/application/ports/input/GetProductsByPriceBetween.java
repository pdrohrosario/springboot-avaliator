package com.project.catalogservice.application.ports.input;

public interface GetProductByPriceBetween {
    List<Product> execute(Double minPrice, Double maxPrice);
}
