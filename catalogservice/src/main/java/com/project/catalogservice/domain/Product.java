package com.project.catalogservice.domain;


import java.math.BigDecimal;
import java.time.LocalDate;
import com.project.catalogservice.domain.enums.ProductCategory;

public class Product {

    private Long id;

    private String name;

    private BigDecimal price;

    private String description;

    private ProductCategory category;

    private ProductStatus status;

    private LocalDate createdAt;

    private Product(Long id, String name, BigDecimal price, String description, ProductCategory category) {
        validateName(name);
        validatePrice(price);
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.status = ProductStatus.AVAILABLE;
        this.createdAt = LocalDate.now();
    }

    public static Product create(Long id, String name, BigDecimal price, String description, String category) {
        return new Product(id, name, price, description, ProductCategory.valueOf(category));
    }

    private void validateName(String name) {
        if(name == null || name.trim().isEmpty() || name.length() > 50) {
            throw new IllegalArgumentException("Name invalid");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price invalid");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
