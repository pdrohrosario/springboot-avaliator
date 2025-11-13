package com.project.catalogservice.config.exception;

import java.time.LocalDateTime;

public record ErrorMessage(String message, String details, LocalDateTime timestamp) {
}
