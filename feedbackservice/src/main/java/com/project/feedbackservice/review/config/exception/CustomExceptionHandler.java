package com.project.feedbackservice.review.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.project.feedbackservice.review.domain.ProductNotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                   WebRequest request) {

        StringBuilder strBuilder = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String message = error.getDefaultMessage();
            strBuilder.append(String.format("%s", message).concat(";"));
        });

        ErrorMessage errorDetails = new ErrorMessage(
                strBuilder.toString(),
                request.getDescription(false),
                LocalDateTime.now());

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ProductNotFoundException.class})
    public ResponseEntity<ErrorMessage> handleProductNotFound(ProductNotFoundException ex,
                                                                   WebRequest request) {

        ErrorMessage errorDetails = new ErrorMessage(
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now());

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
}
