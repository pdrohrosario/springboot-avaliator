package com.project.catalogservice.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Product {

    private Long id;

    private String name;

    private BigDecimal price;

    private String description;

    private ProductCategory category;

    private ProductStatus status;

    private LocalDate createdAt;

    public Product(){}

    private Product(Long id, String name, BigDecimal price, String description, String category) {
        validateName(name);
        validatePrice(price);
        validateCategory(category);
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = ProductCategory.valueOf(category);
        this.status = ProductStatus.AVAILABLE;
        this.createdAt = LocalDate.now();
    }

    public static Product create(String name, BigDecimal price, String description, String category) {
        return new Product(null,name, price, description, category);
    }

    public static Product fromEntity(Long id, String name, BigDecimal price, String description, String category, String status, LocalDate createdAt) {
        Product product = new Product(id, name, price, description, category);
        product.status = ProductStatus.valueOf(status);
        product.createdAt = createdAt;
        return product;
    }

    private void validateName(String name) {
        if(name == null || name.trim().isEmpty() || name.length() > 50) {
            throw new IllegalArgumentException("Name cannot be null, empty or exceed 50 characters");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }

        if(!ProductCategory.contains(category)) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
