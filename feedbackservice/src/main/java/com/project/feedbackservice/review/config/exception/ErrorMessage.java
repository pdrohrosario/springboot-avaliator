package com.project.feedbackservice.review.config.exception;

import java.time.LocalDateTime;

public record ErrorMessage(String message, String details, LocalDateTime timestamp) {
}
