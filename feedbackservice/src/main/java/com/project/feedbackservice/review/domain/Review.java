package com.project.feedbackservice.review.domain;

import com.project.feedbackservice.review.common.domain.AggregateRoot;

import java.time.LocalDate;

public class Review extends AggregateRoot<ReviewId> {

    private ProductId productId;
    private int rating;
    private String comment;
    private LocalDate createdAt;

    private Review(ReviewId reviewId, ProductId productId, int rating, String comment) {
        super(reviewId);
        validateProductId(productId);
        validateRating(rating);
        validateComment(comment);
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDate.now();
    }

    public static Review create(ProductId productId, int rating, String comment){
        return new Review(ReviewId.generate(), productId, rating, comment);
    }

    public static Review fromEntity(ReviewId id, ProductId productId, int rating, String comment, LocalDate createdAt){
        Review review = new Review(id, productId, rating, comment);
        review.createdAt = createdAt;
        return review;
    }

    private void validateProductId(ProductId productId) {
        if (productId == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private void validateComment(String comment) {
        if (comment == null || comment.trim().isEmpty() || comment.length() > 500) {
            throw new IllegalArgumentException("Comment cannot be null, empty or exceed 500 characters");
        }
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public ProductId getProductId() {
        return productId;
    }

    public void setProductId(ProductId productId) {
        this.productId = productId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

}
