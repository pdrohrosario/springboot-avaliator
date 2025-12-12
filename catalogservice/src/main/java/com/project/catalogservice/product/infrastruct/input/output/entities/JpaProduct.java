package com.project.catalogservice.product.infrastruct.input.output.entities;

import com.project.catalogservice.product.domain.ProductCategory;
import com.project.catalogservice.product.domain.ProductStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity()
@Table(name = "product")
public class JpaProduct {

    @Id
    private String id;

    private String name;

    private BigDecimal price;

    private String description;

    @Enumerated
    private ProductCategory category;

    @Enumerated
    private ProductStatus status;

    private LocalDate createdAt;

    public JpaProduct() {
    }

    public JpaProduct(String id, String name, BigDecimal price, String description, ProductCategory category, ProductStatus status, LocalDate createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
