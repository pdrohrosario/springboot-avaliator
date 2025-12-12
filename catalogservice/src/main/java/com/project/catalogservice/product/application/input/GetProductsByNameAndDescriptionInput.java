package com.project.catalogservice.product.application.input;

public record GetProductsByNameAndDescriptionInput(String name, String description, int page, int size, String sortBy) {
}
