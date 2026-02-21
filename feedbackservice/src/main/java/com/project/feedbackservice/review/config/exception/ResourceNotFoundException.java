package com.project.feedbackservice.review.config.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super(String.format("Resource not found."));
    }
}
