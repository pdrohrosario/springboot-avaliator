package com.project.feedbackservice.review.domain;

import com.project.feedbackservice.review.common.domain.ValueObject;

import java.util.UUID;

public class ReviewId extends ValueObject {

    private final UUID value;

    private ReviewId(UUID value) {
        this.value = value;
    }
    public static ReviewId generate() {
        return new ReviewId(UUID.randomUUID());
    }

    public static ReviewId fromString(String value) {
        return new ReviewId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
