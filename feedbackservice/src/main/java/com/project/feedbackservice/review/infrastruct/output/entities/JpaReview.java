package com.project.feedbackservice.review.infrastruct.output.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "review")
public class JpaReview {

    @Id
    private UUID id;

    private UUID productId;

    private int rating;

    private String comment;

    private LocalDate createAt;

    private JpaReview(){}

    public JpaReview(UUID id, UUID productId, int rating, String comment, LocalDate createAt){
        this.id = id;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createAt = createAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDate createAt) {
        this.createAt = createAt;
    }
}
