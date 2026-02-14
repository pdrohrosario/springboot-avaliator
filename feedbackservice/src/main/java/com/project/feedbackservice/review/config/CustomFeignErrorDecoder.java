package com.project.feedbackservice.review.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;

import com.project.feedbackservice.review.config.exception.ApiIntegrationException;
import com.project.feedbackservice.review.config.exception.ResourceNotFoundException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        String responseBody = extractResponseBody(response);

        switch (status) {
            case BAD_REQUEST:
                return new IllegalArgumentException("Invalid request: " + responseBody);
            case UNAUTHORIZED:
                return new SecurityException("Unauthorized access");
            case NOT_FOUND:
                return new ResourceNotFoundException();
            case SERVICE_UNAVAILABLE:
            case INTERNAL_SERVER_ERROR:
            case GATEWAY_TIMEOUT:
                return new ApiIntegrationException();
            default:
                return new Exception("Unexpected error: " + responseBody);
        }
    }

    private String extractResponseBody(Response response) {
        if (response.body() == null) {
            return "No response body";
        }

        try {
            return new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "Error reading response body";
        }
    }
}